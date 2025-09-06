package omnivoxel.client.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.ChunkMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.MeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.generators.MeshDataGenerator;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.ModelEntityMeshData;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.client.network.request.CloseRequest;
import omnivoxel.client.network.request.PlayerUpdateRequest;
import omnivoxel.client.network.request.Request;
import omnivoxel.client.network.util.ByteBufUtils;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.server.PackageID;
import omnivoxel.server.entity.EntityType;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.cache.IDCache;
import omnivoxel.util.log.Logger;
import omnivoxel.util.math.Position3D;
import omnivoxel.util.thread.WorkerThreadPool;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Client {
    private final Map<String, ClientEntity> entities;
    private final byte[] clientID;
    private final ClientWorldDataService worldDataService;
    private final Logger logger;
    private final AtomicBoolean clientRunning = new AtomicBoolean(true);
    private final Queue<Position3D> queuedChunkTasks = new ArrayDeque<>();
    private final ClientWorld world;
    private WorkerThreadPool<MeshDataTask> meshDataGenerators;
    private EventLoopGroup group;
    private Channel channel;
    private long lastFlushedTime = System.currentTimeMillis();

    public Client(byte[] clientID, ClientWorldDataService worldDataService, Logger logger, ClientWorld world) {
        this.clientID = clientID;
        this.worldDataService = worldDataService;
        this.logger = logger;
        this.world = world;
        entities = new ConcurrentHashMap<>();
    }

    private static void sendDoubles(Channel channel, PackageID id, byte[] clientID, double... numbers) {
        if (channel == null) {
            System.err.println("[ERROR] Channel is null! Client may not be connected.");
            return;
        }
        if (!channel.isActive()) {
            System.out.println("[ERROR] Channel is closed! Cannot send data.");
            return;
        }

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(id.ordinal());
        buffer.writeBytes(clientID);
        for (double i : numbers) {
            buffer.writeDouble(i);
        }
        flush(channel, buffer);
    }

    private static void flush(Channel channel, ByteBuf byteBuf) {
        channel.writeAndFlush(byteBuf).addListener(f -> {
            if (!f.isSuccess()) {
                System.err.println("[ERROR] Failed: " + f.cause());
                f.cause().printStackTrace();
            }
        });
    }

    public boolean isClientRunning() {
        return clientRunning.get();
    }

    private void sendBytes(Channel channel, PackageID id, byte[]... bytes) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(id.ordinal());
        for (byte[] bites : bytes) {
            buffer.writeBytes(bites);
        }
        flush(channel, buffer);
    }

    private void sendInts(Channel channel, PackageID id, byte[] clientID, int... numbers) {
        if (channel == null) {
            logger.error(String.format("Failed to send PackageID.%s because channel is null. Client may not be connected.", id.toString()));
            return;
        }
        if (!channel.isActive()) {
            logger.error("Channel is closed. Cannot send data.");
            return;
        }

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(id.ordinal());
        buffer.writeBytes(clientID);
        for (int i : numbers) {
            buffer.writeInt(i);
        }
        flush(channel, buffer);
    }

    void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) throws InterruptedException {
        switch (packageID) {
            case REGISTER_PLAYERS:
                registerPlayers(byteBuf);
                byteBuf.release();
                break;
            case NEW_PLAYER:
                newPlayer(byteBuf);
                byteBuf.release();
                break;
            case CHUNK:
                receiveChunk(byteBuf);
                break;
            case ENTITY_UPDATE:
                updateEntity(byteBuf);
                byteBuf.release();
                break;
            case CLOSE:
                String playerID = ByteUtils.bytesToHex(ByteUtils.getBytes(byteBuf, 8, 32));
                entities.remove(playerID);
                logger.info("Removed Player: " + playerID);
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
            case REGISTER_BLOCK: {
                worldDataService.addBlock(ByteBufUtils.registerBlockFromByteBuf(byteBuf));

                byteBuf.release();
                break;
            }
            default:
                System.err.println("Unexpected package key: " + packageID);
                byteBuf.release();
                break;
        }
    }

    private void updateEntity(ByteBuf byteBuf) {
        String entityID = ByteUtils.bytesToHex(ByteUtils.getBytes(byteBuf, 8, 32));
        ClientEntity entity = entities.get(entityID);
        if (entity == null) {
            System.err.println("Received update for unknown player: " + entityID);
        } else {
            double x = byteBuf.getDouble(44);
            double y = byteBuf.getDouble(52);
            double z = byteBuf.getDouble(60);
            double pitch = byteBuf.getDouble(68);
            double yaw = byteBuf.getDouble(76);

            entity.set(x, y, z, pitch, yaw);

            if (entity.getMesh() != null) {
                entity.getMeshData().setModel(new Matrix4f().identity()
                        .translate((float) x, (float) (y - 0.75f / 2), (float) z)
                        .scale(0.5f)
                        .rotateY((float) -yaw));
                if (!entity.getMesh().getChildren().isEmpty()) {
                    entity.getMesh().getChildren().getFirst().getMeshData().setModel(new Matrix4f().translate(0, 0.75f, 0).rotateX((float) -pitch));
                    entity.getMesh().getChildren().get(1).getMeshData().setModel(new Matrix4f().translate(-0.5f, 0.75f, 0));
                    entity.getMesh().getChildren().get(2).getMeshData().setModel(new Matrix4f().translate(0.5f, 0.75f, 0));
                    entity.getMesh().getChildren().get(3).getMeshData().setModel(new Matrix4f().translate(-0.25f, -0.75f, 0));
                    entity.getMesh().getChildren().get(4).getMeshData().setModel(new Matrix4f().translate(0.25f, -0.75f, 0));
                }
            }
        }
    }

    private void receiveChunk(ByteBuf byteBuf) throws InterruptedException {
        int x = byteBuf.getInt(8);
        int y = byteBuf.getInt(12);
        int z = byteBuf.getInt(16);
        byteBuf.retain();
        Position3D position3D = new Position3D(x, y, z);

//        Chunk chunk = ChunkFactory.create(Arrays.stream(blocks).map(Block::getBlock).toArray(omnivoxel.world.block.Block[]::new), palette);
//        world.add(omnivoxel.math.Position3D.createFrom(position3D), chunk);

        meshDataGenerators.submit(new ChunkMeshDataTask(byteBuf, position3D));
    }

    private void loadPlayer(byte[] playerID, String name) throws InterruptedException {
        String id = ByteUtils.bytesToHex(playerID);
        ClientEntity playerEntity = new ClientEntity(name, id, new EntityType(EntityType.Type.PLAYER, name));
        entities.put(id, playerEntity);

        logger.info("Added player: " + id);

        meshDataGenerators.submit(new EntityMeshDataTask(playerEntity));
    }

    private void newPlayer(ByteBuf byteBuf) throws InterruptedException {
        loadPlayer(ByteUtils.getBytes(byteBuf, 8, 32), "Other client!!");
    }

    private void newEntity(ByteBuf byteBuf) throws InterruptedException {
        int entityIDLength = byteBuf.getInt(8);

        byte[] entityID = new byte[entityIDLength];
        byteBuf.getBytes(12, entityID);

        int doubleStart = 12 + entityIDLength;
        double x = byteBuf.getDouble(doubleStart);
        double y = byteBuf.getDouble(doubleStart + Double.BYTES);
        double z = byteBuf.getDouble(doubleStart + Double.BYTES * 2);
        double pitch = byteBuf.getDouble(doubleStart + Double.BYTES * 3);
        double yaw = byteBuf.getDouble(doubleStart + Double.BYTES * 4);

        int nameLength = byteBuf.getInt(doubleStart + Double.BYTES * 5);
        int nameStart = doubleStart + Double.BYTES * 5;
        byte[] nameBytes = new byte[nameLength];
        byteBuf.getBytes(nameStart, nameBytes);
        String name = new String(nameBytes);

        int typeOrdinal = byteBuf.getInt(nameStart + nameLength);
        EntityType.Type type = EntityType.Type.values()[typeOrdinal];

        String id = ByteUtils.bytesToHex(entityID);
        ClientEntity entity = new ClientEntity(name, id, new EntityType(type, name));
        entity.set(x, y, z, pitch, yaw);
        entity.setMeshData(new ModelEntityMeshData(entity).setModel(new Matrix4f().translate((float) x, (float) y, (float) z)));
        entities.put(id, entity);

        meshDataGenerators.submit(new EntityMeshDataTask(entity));
    }

    private void registerPlayers(ByteBuf byteBuf) throws InterruptedException {
        int playerCount = Math.floorDiv(byteBuf.readableBytes(), 32);
        for (int i = 0; i < playerCount; i++) {
            loadPlayer(ByteUtils.getBytes(byteBuf, i * 32 + 8, 32), "Other client that was already here!!");
        }
    }

    public Map<String, ClientEntity> getEntities() {
        return entities;
    }

    public void tick() {
        long time = System.currentTimeMillis();
        if (time - lastFlushedTime > ConstantServerSettings.CHUNK_REQUEST_BATCHING_TIME || queuedChunkTasks.size() > ConstantServerSettings.CHUNK_REQUEST_BATCHING_LIMIT) {
            int[] data = new int[queuedChunkTasks.size() * 3 + 1];
            data[0] = queuedChunkTasks.size();
            for (int i = 0; !queuedChunkTasks.isEmpty(); i++) {
                Position3D req = queuedChunkTasks.remove();
                data[i * 3 + 1] = req.x();
                data[i * 3 + 2] = req.y();
                data[i * 3 + 3] = req.z();
            }
            sendInts(channel, PackageID.CHUNK_REQUEST, clientID, data);
            lastFlushedTime += ConstantServerSettings.CHUNK_REQUEST_BATCHING_TIME;
        }
    }

    public void sendRequest(Request request) {
        switch (request.getType()) {
            case CHUNK:
                Position3D position3D = ((ChunkRequest) request).position3D();
                queuedChunkTasks.add(position3D);
                break;
            case CLOSE:
                sendBytes(channel, PackageID.CLOSE, clientID);
                break;
            case PLAYER_UPDATE:
                PlayerUpdateRequest r = (PlayerUpdateRequest) request;
                sendDoubles(channel, PackageID.PLAYER_UPDATE, clientID, r.x(), r.y(), r.z(), r.pitch(), r.yaw());
                break;
            default:
                System.err.println("Unexpected request type: " + request.getType());
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
        logger.debug("Disconnecting from server...");
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
            meshDataGenerators.shutdown();
            meshDataGenerators.awaitTermination();
        }
        logger.info("Client disconnected");
    }

    // TODO: This is messy. Fix it
    public void setListeners(IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<String> queuedEntityMeshData) {
        meshDataGenerators = new WorkerThreadPool<>(
                ConstantGameSettings.MAX_MESH_GENERATOR_THREADS,
                new MeshDataGenerator(
                        worldDataService,
                        entityMeshDefinitionCache,
                        queuedEntityMeshData,
                        world
                )::generateMeshData,
                true
        );
    }
}