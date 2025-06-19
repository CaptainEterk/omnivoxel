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
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.client.network.request.CloseRequest;
import omnivoxel.client.network.request.PlayerUpdateRequest;
import omnivoxel.client.network.request.Request;
import omnivoxel.math.Position3D;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.server.PackageID;
import omnivoxel.server.entity.EntityType;
import omnivoxel.util.cache.IDCache;
import omnivoxel.util.log.Logger;
import omnivoxel.util.thread.WorkerThreadPool;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Client {
    private final Map<String, ClientEntity> entities;
    private final byte[] clientID;
    private final ClientWorldDataService worldDataService;
    private final Logger logger;
    private final AtomicBoolean clientRunning = new AtomicBoolean(true);
    private final Queue<Position3D> queuedChunkTasks = new ArrayDeque<>();
    private WorkerThreadPool<MeshDataTask> meshDataGenerators;
    private EventLoopGroup group;
    private Channel channel;
    private long lastFlushedTime = System.currentTimeMillis();

    public Client(byte[] clientID, ClientWorldDataService worldDataService, Logger logger) {
        this.clientID = clientID;
        this.worldDataService = worldDataService;
        this.logger = logger;
        entities = new ConcurrentHashMap<>();
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
        for (int i : numbers) {
            buffer.writeInt(i);
        }
        flush(channel, buffer);
    }

    private void sendFloats(Channel channel, PackageID id, byte[] clientID, float... numbers) {
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
        for (float i : numbers) {
            buffer.writeFloat(i);
        }
        flush(channel, buffer);
    }

    private void flush(Channel channel, ByteBuf byteBuf) {
        channel.writeAndFlush(byteBuf).addListener(f -> {
            if (!f.isSuccess()) {
                System.err.println("[ERROR] Failed: " + f.cause());
                f.cause().printStackTrace();
            }
        });
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
                String playerID = bytesToHex(getBytes(byteBuf, 8, 32));
                entities.remove(playerID);
                logger.info("Removed Player: " + playerID);
                byteBuf.release();
                break;
            default:
                System.err.println("Unexpected package id: " + packageID);
        }
    }

    private void updateEntity(ByteBuf byteBuf) {
        String entityID = bytesToHex(byteBuf, 8, 32);
        ClientEntity entity = entities.get(entityID);
        if (entity == null) {
            System.err.println("Received update for unknown player: " + entityID);
        } else {
            float x = Float.intBitsToFloat(byteBuf.getInt(44));
            float y = Float.intBitsToFloat(byteBuf.getInt(48));
            float z = Float.intBitsToFloat(byteBuf.getInt(52));
            float pitch = Float.intBitsToFloat(byteBuf.getInt(56));
            float yaw = Float.intBitsToFloat(byteBuf.getInt(60));
            entity.setX(x);
            entity.setY(y);
            entity.setZ(z);
            entity.setPitch(pitch);
            entity.setYaw(yaw);
            if (entity.getMesh() != null) {
                Matrix4f model = new Matrix4f().identity()
                        .translate(x, y, z)
                        .rotateY(-yaw)
                        .rotateX(-pitch);

                entity.getMesh().setModel(model);

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
        ClientEntity playerEntity = new ClientEntity(name, bytesToHex(playerID), new EntityType(EntityType.Type.PLAYER, name));
        String id = bytesToHex(playerID);
        entities.put(id, playerEntity);

        System.out.println("Added player: " + id);

        meshDataGenerators.submit(new EntityMeshDataTask(playerEntity));
    }

    private void newPlayer(ByteBuf byteBuf) throws InterruptedException {
        // TODO: Add more information to the player packet, such as name, position, etc...
        loadPlayer(getBytes(byteBuf, 8, 32), "Other client!!");
    }

    private void registerPlayers(ByteBuf byteBuf) throws InterruptedException {
        int playerCount = Math.floorDiv(byteBuf.readableBytes(), 32);
        for (int i = 0; i < playerCount; i++) {
            // TODO: Add more information to the player packet, such as name, position, etc...
            loadPlayer(getBytes(byteBuf, i * 32 + 8, 32), "Other client that was already here!!");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    private String bytesToHex(ByteBuf byteBuf, int start, int length) {
        byte[] bytes = getBytes(byteBuf, start, length);
        return bytesToHex(bytes);
    }

    private byte[] getBytes(ByteBuf byteBuf, int start, int length) {
        byte[] bytes = new byte[length];
        byteBuf.getBytes(start, bytes);
        return bytes;
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
                sendFloats(channel, PackageID.PLAYER_UPDATE, clientID, r.x(), r.y(), r.z(), r.pitch(), r.yaw());
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
        logger.info("Client shutting down...");
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
        logger.info("Client shutdown");
    }

    public void setListeners(BiConsumer<Position3D, MeshData> loadChunk, Consumer<ClientEntity> loadEntity, IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<String> queuedEntityMeshData) {
        meshDataGenerators = new WorkerThreadPool<>(ConstantGameSettings.MAX_MESH_GENERATOR_THREADS, new MeshDataGenerator(loadChunk, loadEntity, worldDataService, entityMeshDefinitionCache, queuedEntityMeshData)::generateMeshData);
    }
}