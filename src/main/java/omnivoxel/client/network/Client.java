package omnivoxel.client.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import omnivoxel.client.game.entity.EntityMeshWrapper;
import omnivoxel.client.game.graphics.api.opengl.mesh.MeshDataTask;
import omnivoxel.client.game.graphics.api.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.api.opengl.mesh.generators.MeshDataGenerator;
import omnivoxel.client.game.graphics.api.opengl.mesh.generators.lighting.ChunkMeshDataLightingGenerator;
import omnivoxel.client.game.graphics.api.opengl.mesh.tasks.EntityMeshDataTask;
import omnivoxel.client.game.graphics.api.opengl.mesh.tasks.LightingChunkMeshDataTask;
import omnivoxel.client.game.graphics.block.BlockWithMesh;
import omnivoxel.client.game.player.PlayerController;
import omnivoxel.client.game.state.State;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.game.world.ClientWorldChunk;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.client.network.request.*;
import omnivoxel.client.network.util.ByteBufUtils;
import omnivoxel.common.network.NetworkService;
import omnivoxel.common.network.NetworkUser;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.common.settings.ConstantNetworkSettings;
import omnivoxel.common.settings.Settings;
import omnivoxel.server.PackageID;
import omnivoxel.server.entity.Entity;
import omnivoxel.server.entity.EntityType;
import omnivoxel.server.io.entity.EntityIO;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.cache.IDCache;
import omnivoxel.util.log.Logger;
import omnivoxel.util.math.Position2D;
import omnivoxel.util.math.Position3D;
import omnivoxel.util.thread.WorkerThreadPool;
import omnivoxel.world.block.Block;
import omnivoxel.world.block.BlockService;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk2d.Chunk2D;
import omnivoxel.world.chunk2d.SingleBlockChunk2D;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: Clean this up
public final class Client implements NetworkUser {
    private final byte[] clientID;
    private final ClientWorldDataService worldDataService;
    private final AtomicBoolean clientRunning = new AtomicBoolean(true);
    private final Queue<Position3D> queuedChunkTasks = new LinkedBlockingDeque<>();
    private final ClientWorld world;
    private final BlockService<BlockWithMesh> blockService;
    private final Settings settings;
    private final Map<Position3D, Block> pendingBlocks;
    private WorkerThreadPool<MeshDataTask> meshDataGenerators;
    private WorkerThreadPool<LightingChunkMeshDataTask> lightingGenerators;
    private EventLoopGroup group;
    private Channel channel;
    private long lastFlushedTime = System.currentTimeMillis();
    private PlayerController player = null;

    public Client(byte[] clientID, ClientWorldDataService worldDataService, ClientWorld world, BlockService<BlockWithMesh> blockService, Settings settings) {
        this.clientID = clientID;
        this.worldDataService = worldDataService;
        this.world = world;
        this.blockService = blockService;
        this.settings = settings;
        pendingBlocks = new HashMap<>();
    }

    public boolean isClientRunning() {
        return clientRunning.get();
    }

    @Override
    public void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) {
        try {
            switch (packageID) {
                case CHUNK:
                    receiveChunk(byteBuf);
                    break;
                case HEIGHTS:
                    receiveChunkHeights(byteBuf);
                    break;
                case ENTITY_UPDATE:
                    updateEntity(byteBuf);
                    byteBuf.release();
                    break;
                case CLOSE:
                    String playerID = ByteUtils.bytesToHex(ByteUtils.getBytes(byteBuf, 8, 32));
                    world.removeEntity(playerID);
                    Logger.info("Removed Player: " + playerID);
                    world.removeEntity(playerID);
                    byteBuf.release();
                    break;
                case NEW_ENTITY:
                    newEntity(byteBuf);
                    byteBuf.release();
                    break;
                case REGISTER_BLOCK_SHAPE:
                    ByteBufUtils.cacheBlockShapeFromByteBuf(byteBuf);
                    byteBuf.release();
                    break;
                case REGISTER_BLOCK_HITBOX:
                    ByteBufUtils.cacheBlockHitboxFromByteBuf(byteBuf);
                    byteBuf.release();
                    break;
                case REGISTER_BLOCK:
                    worldDataService.addBlock(ByteBufUtils.registerBlockFromByteBuf(byteBuf));
                    byteBuf.release();
                    break;
                case REPLACE_BLOCK:
                    int replacedBlocks = byteBuf.getInt(8);
                    int index = 12;
                    for (int i = 0; i < replacedBlocks; i++) {
                        int worldX = byteBuf.getInt(index);
                        int worldY = byteBuf.getInt(index + 4);
                        int worldZ = byteBuf.getInt(index + 8);
                        index += 12;

                        int highestY = byteBuf.getByte(index);
                        index += 4;

                        byte rotation = (byte) (byteBuf.getByte(index++) & 3);

                        short paletteLength = byteBuf.getShort(index);
                        index += 2;

                        byte[] idBytes = new byte[paletteLength];
                        byteBuf.getBytes(index, idBytes);
                        String blockID = new String(idBytes);
                        index += paletteLength;

                        replaceBlock(worldX, worldY, worldZ, highestY, blockID, rotation);
                    }

                    byteBuf.release();
                    break;
                case PLAYER_STATE:
                    double[] data = new double[5];
                    for (int i = 0; i < 5; i++) {
                        data[i] = byteBuf.getDouble(8 + i * Double.BYTES);
                    }
                    double x = data[0];
                    double y = data[1];
                    double z = data[2];
                    double pitch = data[3];
                    double yaw = data[4];
                    player.set(x, y, z, pitch, yaw);
                    byteBuf.release();
                    break;
                default:
                    Logger.error(Logger.Priority.HIGH, "Unexpected package key: " + packageID);
                    byteBuf.release();
                    break;
            }
        } catch (RuntimeException e) {
            byteBuf.release();
            clientRunning.set(false);
            throw e;
        }
    }

    private void replaceBlock(int worldX, int worldY, int worldZ, int highestY, String blockID, byte rotation) {
        int chunkX = Math.floorDiv(worldX, ConstantCommonSettings.CHUNK_WIDTH);
        int chunkY = Math.floorDiv(worldY, ConstantCommonSettings.CHUNK_HEIGHT);
        int chunkZ = Math.floorDiv(worldZ, ConstantCommonSettings.CHUNK_LENGTH);

        int x = Math.floorMod(worldX, ConstantCommonSettings.CHUNK_WIDTH);
        int y = Math.floorMod(worldY, ConstantCommonSettings.CHUNK_HEIGHT);
        int z = Math.floorMod(worldZ, ConstantCommonSettings.CHUNK_LENGTH);
        Position2D chunkPosition2D = new Position2D(chunkX, chunkZ);
        Chunk2D<Integer> skylightChunk = world.getChunkHeights(chunkPosition2D);
        if (skylightChunk != null) {
            world.setChunkHeights(chunkPosition2D, skylightChunk.setBlock(x, z, highestY));
        }

        Position3D chunkPosition = new Position3D(chunkX, chunkY, chunkZ);

        ClientWorldChunk clientWorldChunk = world.get(chunkPosition, false, false);
        if (clientWorldChunk != null) {
            Chunk<BlockWithMesh> chunkData = clientWorldChunk.getChunkData();

            if (chunkData != null) {
                clientWorldChunk.setCleanLighting(false);
                BlockWithMesh block = blockService.getBlock(blockID);
                if (chunkData.getBlock(x, y, z) != block || chunkData.getBlockRotation(x, y, z) != rotation) {
                    clientWorldChunk.setChunkData(chunkData.setBlock(x, y, z, block, rotation));
                    lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition));

                    if (x == 0)
                        lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition.add(-1, 0, 0)));
                    if (x == ConstantCommonSettings.CHUNK_WIDTH - 1)
                        lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition.add(1, 0, 0)));

                    if (y == 0)
                        lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition.add(0, -1, 0)));
                    if (y == ConstantCommonSettings.CHUNK_HEIGHT - 1)
                        lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition.add(0, 1, 0)));

                    if (z == 0)
                        lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition.add(0, 0, -1)));
                    if (z == ConstantCommonSettings.CHUNK_LENGTH - 1)
                        lightingGenerators.submit(new LightingChunkMeshDataTask(null, chunkPosition.add(0, 0, 1)));
                }
            }
        }
    }

    private void updateEntity(ByteBuf byteBuf) {
        int offset = 8;

        // entity type
        int entityType = byteBuf.getInt(offset);
        offset += Integer.BYTES;

        int idLength = byteBuf.getInt(offset);
        offset += Integer.BYTES;

        byte[] entityIDBytes = new byte[idLength];
        byteBuf.getBytes(offset, entityIDBytes);
        offset += idLength;

        String entityID = new String(entityIDBytes);

        EntityMeshWrapper entityMeshWrapper = world.getEntity(entityID);
        if (entityMeshWrapper == null) {
            Logger.warn(Logger.Priority.NORMAL, "Received update for unknown entity: " + entityID);
            return;
        }

        double x = byteBuf.getDouble(offset);
        offset += Double.BYTES;

        double y = byteBuf.getDouble(offset);
        offset += Double.BYTES;

        double z = byteBuf.getDouble(offset);
        offset += Double.BYTES;

        double pitch = byteBuf.getDouble(offset);
        offset += Double.BYTES;

        double yaw = byteBuf.getDouble(offset);

        Entity entity = entityMeshWrapper.entity();

        entity.set(x, y, z, pitch, yaw);

        if (entity.getMesh() != null) {
            entityMeshWrapper.getMeshData().setModel(
                    new Matrix4f()
                            .identity()
                            .translate((float) x, (float) (y - 0.75f / 2), (float) z)
                            .scale(0.5f)
                            .rotateY((float) -yaw)
            );

            if (!entity.getMesh().getChildren().isEmpty()) {
                entity.getMesh().getChildren().getFirst()
                        .getMeshData()
                        .setModel(
                                new Matrix4f()
                                        .translate(0, 0.75f, 0)
                                        .rotateX((float) -pitch)
                        );

                entity.getMesh().getChildren().get(1)
                        .getMeshData()
                        .setModel(new Matrix4f().translate(-0.5f, 0.75f, 0));

                entity.getMesh().getChildren().get(2)
                        .getMeshData()
                        .setModel(new Matrix4f().translate(0.5f, 0.75f, 0));

                entity.getMesh().getChildren().get(3)
                        .getMeshData()
                        .setModel(new Matrix4f().translate(-0.25f, -0.75f, 0));

                entity.getMesh().getChildren().get(4)
                        .getMeshData()
                        .setModel(new Matrix4f().translate(0.25f, -0.75f, 0));
            }
        } else if (!world.isEntityMeshDataQueued(EntityType.values()[entityType])) {
            Logger.warn("No mesh found for entity: " + entityID);
        }
    }

    private void receiveChunk(ByteBuf byteBuf) {
        int x = byteBuf.getInt(8);
        int y = byteBuf.getInt(12);
        int z = byteBuf.getInt(16);
        Position3D position3D = new Position3D(x, y, z);

        world.receivedChunk(position3D);

        lightingGenerators.submit(new LightingChunkMeshDataTask(byteBuf, position3D));
    }

    private void receiveChunkHeights(ByteBuf byteBuf) {
        int cx = byteBuf.getInt(8);
        int cz = byteBuf.getInt(12);
        Chunk2D<Integer> chunkHeights = new SingleBlockChunk2D<>(0);
        int x = 0, z = 0;
        for (int i = 0; i < ConstantCommonSettings.BLOCKS_IN_CHUNK_2D; i++) {
            chunkHeights = chunkHeights.setBlock(x, z, byteBuf.getInt(16 + i * Integer.BYTES));
            x++;
            if (x >= ConstantCommonSettings.CHUNK_WIDTH) {
                x = 0;
                z++;
            }
        }
        world.setChunkHeights(new Position2D(cx, cz), chunkHeights);
        byteBuf.release();
    }

    private void newEntity(ByteBuf byteBuf) {
        byteBuf.readerIndex(8);
        int entityID = byteBuf.readInt();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        Entity entity = EntityIO.decode(entityID, bytes);

        EntityMeshWrapper entityMeshWrapper = new EntityMeshWrapper(entity);
        world.addEntity(entityMeshWrapper);
        meshDataGenerators.submit(new EntityMeshDataTask(entityMeshWrapper));
    }

    public void tick() {
        if (!clientRunning.get()) {
            return;
        }
        long time = System.currentTimeMillis();
        if (time - lastFlushedTime > ConstantNetworkSettings.CHUNK_REQUEST_BATCHING_TIME || queuedChunkTasks.size() > ConstantNetworkSettings.CHUNK_REQUEST_BATCHING_LIMIT) {
            List<Position3D> queuedChunkTasksBatch = new ArrayList<>();
            while (!queuedChunkTasks.isEmpty()) {
                Position3D position3D = queuedChunkTasks.remove();
                if (position3D != null) {
                    queuedChunkTasksBatch.add(position3D);
                } else {
                    break;
                }
            }
            if (!queuedChunkTasksBatch.isEmpty()) {
                int[] data = new int[queuedChunkTasksBatch.size() * 3 + 1];
                data[0] = queuedChunkTasksBatch.size();
                for (int i = 0; !queuedChunkTasksBatch.isEmpty(); i++) {
                    Position3D req = queuedChunkTasksBatch.removeFirst();

                    data[i * 3 + 1] = req.x();
                    data[i * 3 + 2] = req.y();
                    data[i * 3 + 3] = req.z();
                }
                NetworkService.sendInts(channel, PackageID.CHUNK_REQUEST, clientID, data);
            }
            lastFlushedTime += ConstantNetworkSettings.CHUNK_REQUEST_BATCHING_TIME;
        }
    }

    public void sendRequest(Request request) {
        switch (request.getType()) {
            case CHUNK:
                Position3D position3D = ((ChunkRequest) request).position3D();
                queuedChunkTasks.add(position3D);
                break;
            case CLOSE:
                NetworkService.sendBytes(channel, PackageID.CLOSE, clientID);
                break;
            case PLAYER_UPDATE:
                PlayerUpdateRequest playerUpdateRequest = (PlayerUpdateRequest) request;
                NetworkService.sendDoubles(channel, PackageID.PLAYER_UPDATE, clientID, playerUpdateRequest.x(), playerUpdateRequest.y(), playerUpdateRequest.z(), playerUpdateRequest.pitch(), playerUpdateRequest.yaw());
                break;
            case BLOCK_REPLACE:
                BlockReplaceRequest blockReplaceRequest = (BlockReplaceRequest) request;
                byte[] bytes = new byte[Integer.BYTES * 4 + 1 + blockReplaceRequest.newBlock().id().length()];
                ByteUtils.addInt(bytes, blockReplaceRequest.position3D().x(), 0);
                ByteUtils.addInt(bytes, blockReplaceRequest.position3D().y(), Integer.BYTES);
                ByteUtils.addInt(bytes, blockReplaceRequest.position3D().z(), Integer.BYTES * 2);
                ByteUtils.addInt(bytes, blockReplaceRequest.newBlock().id().length(), Integer.BYTES * 3);
                bytes[Integer.BYTES * 4] = (byte) (blockReplaceRequest.rotation() & 3);
                System.arraycopy(blockReplaceRequest.newBlock().id().getBytes(), 0, bytes, Integer.BYTES * 4 + 1, blockReplaceRequest.newBlock().id().length());
                NetworkService.sendBytes(channel, PackageID.REPLACE_BLOCK, clientID, bytes);

                // TODO: Calculate highestY
                pendingBlocks.put(blockReplaceRequest.position3D(), blockReplaceRequest.oldBlock());
                replaceBlock(blockReplaceRequest.position3D().x(), blockReplaceRequest.position3D().y(), blockReplaceRequest.position3D().z(), 0, blockReplaceRequest.newBlock().id(), blockReplaceRequest.rotation());
                break;
            default:
                Logger.error(Logger.Priority.HIGH, "Unexpected request type: " + request.getType());
        }
    }

    byte[] getClientID() {
        return clientID;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setGroup(EventLoopGroup group) {
        this.group = group;
    }

    public void close() {
        Logger.debug("Disconnecting from server...");
        sendRequest(new CloseRequest());
        try {
            if (channel != null) {
                channel.close().sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (group != null) {
                group.shutdownGracefully();
            }
            clientRunning.set(false);
            lightingGenerators.shutdown();
            meshDataGenerators.shutdown();
            lightingGenerators.awaitTermination();
            meshDataGenerators.awaitTermination();
        }
        Logger.info("Client disconnected");
    }

    public void setListeners(IDCache<EntityType, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<EntityType> queuedEntityMeshData, State state) {
        meshDataGenerators = new WorkerThreadPool<>(
                settings.getIntSetting("max_mesh_generator_threads", Runtime.getRuntime().availableProcessors()),
                () -> new MeshDataGenerator(
                        worldDataService,
                        entityMeshDefinitionCache,
                        queuedEntityMeshData,
                        world,
                        blockService,
                        state,
                        settings
                )::generateMeshData,
                true
        );
        lightingGenerators = new WorkerThreadPool<>(
                settings.getIntSetting("max_lighting_generator_threads", Runtime.getRuntime().availableProcessors()),
                () -> new ChunkMeshDataLightingGenerator(
                        world,
                        worldDataService,
                        meshDataGenerators,
                        blockService,
                        state
                )::generateLightingMeshData,
                true
        );
    }

    public void setPlayer(PlayerController player) {
        if (this.player == null) {
            this.player = player;
        } else {
            throw new IllegalArgumentException("Cannot set player twice");
        }
    }
}
