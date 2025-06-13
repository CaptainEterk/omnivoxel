package omnivoxel.client.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import omnivoxel.client.game.entity.mob.player.PlayerEntity;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.thread.mesh.MeshDataGenerator;
import omnivoxel.client.game.thread.mesh.MeshDataTask;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.client.network.request.CloseRequest;
import omnivoxel.client.network.request.PlayerUpdateRequest;
import omnivoxel.client.network.request.Request;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.server.PackageID;
import omnivoxel.math.Position3D;
import omnivoxel.util.log.Logger;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class Client {
    private final Map<String, PlayerEntity> players;
    private final byte[] clientID;
    private final ClientWorldDataService worldDataService;
    private final Logger logger;
    private final Set<BlockingQueue<MeshDataTask>> meshDataTaskQueues;
    private final AtomicBoolean clientRunning = new AtomicBoolean(true);
    private final Queue<Position3D> queuedChunkTasks = new ArrayDeque<>();
    private ExecutorService meshDataGenerators;
    private EventLoopGroup group;
    private Channel channel;
    private long lastFlushedTime = System.currentTimeMillis();

    public Client(byte[] clientID, ClientWorldDataService worldDataService, Logger logger) {
        this.clientID = clientID;
        this.worldDataService = worldDataService;
        this.logger = logger;
        players = new ConcurrentHashMap<>();
        meshDataTaskQueues = ConcurrentHashMap.newKeySet();
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
            case PLAYER_UPDATE:
                updatePlayer(byteBuf);
                byteBuf.release();
                break;
            default:
                System.err.println("Unexpected package id: " + packageID);
        }
    }

    private void updatePlayer(ByteBuf byteBuf) {
        String playerID = bytesToHex(byteBuf, 8, 32);
        PlayerEntity playerEntity = players.get(playerID);
        float x = Float.intBitsToFloat(byteBuf.getInt(40));
        float y = Float.intBitsToFloat(byteBuf.getInt(44));
        float z = Float.intBitsToFloat(byteBuf.getInt(48));
        float pitch = Float.intBitsToFloat(byteBuf.getInt(52));
        float yaw = Float.intBitsToFloat(byteBuf.getInt(56));
        System.out.println(x + " " + y + " " + z + " " + pitch + " " + yaw);
        playerEntity.setX(x);
        playerEntity.setY(y);
        playerEntity.setZ(z);
        playerEntity.setPitch(pitch);
        playerEntity.setYaw(yaw);
    }

    private void receiveChunk(ByteBuf byteBuf) throws InterruptedException {
        int x = byteBuf.getInt(8);
        int y = byteBuf.getInt(12);
        int z = byteBuf.getInt(16);
        Position3D position3D = new Position3D(x, y, z);

//        Chunk chunk = ChunkFactory.create(Arrays.stream(blocks).map(Block::getBlock).toArray(omnivoxel.world.block.Block[]::new), palette);
//        world.add(omnivoxel.math.Position3D.createFrom(position3D), chunk);

        BlockingQueue<MeshDataTask> smallestQueue = null;
        int smallestSize = Integer.MAX_VALUE;
        for (BlockingQueue<MeshDataTask> queue : meshDataTaskQueues) {
            int size = queue.size();
            if (size < smallestSize) {
                smallestQueue = queue;
                smallestSize = size;
                if (size == 0) {
                    break;
                }
            }
        }
        if (smallestQueue != null) {
            smallestQueue.put(new MeshDataTask(byteBuf, position3D));
        }
    }

    private void loadPlayer(byte[] playerID, String name) {
//        PlayerEntity playerEntity = new PlayerEntity(name, playerID);
//        String id = bytesToHex(playerID);
//        players.put(id, playerEntity);
        // Generate mesh data
//        MeshData meshData = meshDataGenerator.generateEntityMeshData(playerEntity);
//        playerEntity.setMeshData(meshData);
        //
//        loadEntity.accept(id, playerEntity);
    }

    private void newPlayer(ByteBuf byteBuf) {
        // TODO: Add more information to the player packet, such as name, position, etc...
        loadPlayer(getBytes(byteBuf, 8, 32), "Other client!!");
    }

    private void registerPlayers(ByteBuf byteBuf) {
        int playerCount = Math.floorDiv(byteBuf.readableBytes(), 32);
        for (int i = 0; i < playerCount; i++) {
            // TODO: Add more information to the player packet, such as name, position, etc...
            loadPlayer(getBytes(byteBuf, i * 32 + 8, 32), "Other client that was already here!!");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < bytes[i]; i++) {
            hex.append(String.format("%02X", bytes[i]));
        }
        return hex.toString();
    }

    private String bytesToHex(ByteBuf byteBuf, int start, int length) {
        return bytesToHex(getBytes(byteBuf, start, length));
    }

    private byte[] getBytes(ByteBuf byteBuf, int start, int length) {
        byte[] bytes = new byte[length];
        byteBuf.getBytes(start, bytes);
        return bytes;
    }

    public Map<String, PlayerEntity> getPlayers() {
        return players;
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
            lastFlushedTime = ConstantServerSettings.CHUNK_REQUEST_BATCHING_TIME;
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
            meshDataGenerators.close();
        }
        System.out.println("Client shutdown");
    }

    public void setChunkListener(BiConsumer<Position3D, MeshData> loadChunk) {
        meshDataGenerators = Executors.newFixedThreadPool(ConstantGameSettings.MAX_MESH_GENERATOR_THREADS);
        for (int i = 0; i < ConstantGameSettings.MAX_MESH_GENERATOR_THREADS; i++) {
            MeshDataGenerator meshDataGenerator = new MeshDataGenerator(logger, loadChunk, clientRunning, worldDataService);
            Thread meshDataGeneratorThread = new Thread(meshDataGenerator);
            meshDataGenerators.execute(meshDataGeneratorThread);
            meshDataTaskQueues.add(meshDataGenerator.getMeshDataTasks());
        }
    }
}