package omnivoxel.server;

import core.biomes.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.math.Position3D;
import omnivoxel.server.client.ServerPlayer;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.ChunkTask;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;
import omnivoxel.util.thread.WorkerThreadPool;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final int VERSION_ID = 0;

    private final Map<String, ServerPlayer> clients;
    private final AtomicLong requestReceived = new AtomicLong();
    private final AtomicLong requestSent = new AtomicLong();

    private final WorkerThreadPool<ChunkTask> workerThreadPool;

    public Server(long seed, ServerWorld world) throws IOException {
        this.clients = new ConcurrentHashMap<>();

//        Path worldPath = Paths.get("run/.worlds");
//
//        try (Stream<Path> pathStream = Files.walk(worldPath)) {
//            pathStream.sorted(Comparator.reverseOrder()) // Delete files before the directory itself
//                    .forEach(path -> {
//                        try {
//                            Files.delete(path);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    });
//
//            // Recreate the directory
//            Files.createDirectories(worldPath);
//        }

        Map<Position3D, ServerBlock> queuedBlocks = new ConcurrentHashMap<>();
        BlockService blockService = new BlockService();

        BiomeService biomeService = new BiomeService(Map.of(new ClimateVector(0.0, 0.0, 0.7, 0.3, 0.0), new DesertBiome(blockService), new ClimateVector(0.0, 0.0, 0.7, 0.7, 0.0), new JungleBiome(blockService), new ClimateVector(0.0, 0.0, 0.3, 0.3, 0.0), new TundraBiome(blockService), new ClimateVector(0.0, 0.0, 0.3, 0.7, 0.0), new TaigaBiome(blockService), new ClimateVector(0.0, 0.0, 0.5, 0.5, 0.0), new PlainsBiome(blockService)));
        workerThreadPool = new WorkerThreadPool<>(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT, new ChunkGenerator(new BasicWorldDataService(new Random(seed), world, biomeService, blockService, queuedBlocks), blockService, biomeService, world)::generateChunk);
    }

    private static void sendBytes(ChannelHandlerContext ctx, PackageID id, byte[]... bytes) {
        ByteBuf buffer = Unpooled.buffer();
        int length = 4;
        for (byte[] bites : bytes) {
            length += bites.length;
        }
        buffer.writeInt(length);
        buffer.writeInt(id.ordinal());
        for (byte[] bites : bytes) {
            buffer.writeBytes(bites);
        }
        ctx.channel().writeAndFlush(buffer);
    }

    public void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) throws InterruptedException {
        String clientID = bytesToHex(byteBuf, 4, 32);
        switch (packageID) {
            case CHUNK_REQUEST:
                int count = byteBuf.getInt(36);
                for (int i = 0; i < count; i++) {
                    int x = byteBuf.getInt(i * 3 * Integer.BYTES + 40);
                    int y = byteBuf.getInt(i * 3 * Integer.BYTES + 44);
                    int z = byteBuf.getInt(i * 3 * Integer.BYTES + 48);
//                    System.out.printf("Chunk Request: {%d, %d, %d}", x, y, z);
                    queueChunkTask(new ChunkTask(ctx, x, y, z));
                }
                break;
            case REGISTER_CLIENT:
                registerClient(ctx, byteBuf);
                break;
            case CLOSE:
                clients.remove(clientID);
                break;
            case PLAYER_UPDATE:
                float[] data = new float[5];
                for (int i = 0; i < 5; i++) {
                    data[i] = byteBuf.getFloat(36 + i * Float.BYTES);
                }
                float x = data[0];
                float y = data[1];
                float z = data[2];
                float pitch = data[3];
                float yaw = data[4];
                System.out.printf("Player Update Packet {x:%.2f, y: %.2f, z: %.2f, pitch: %.2f, yaw: %.2f}\n", x, y, z, pitch, yaw);
                break;
            default:
                System.err.println("Unknown package id: " + packageID);
        }
    }

    private void queueChunkTask(ChunkTask chunkTask) throws InterruptedException {
        workerThreadPool.submit(chunkTask);
//        long requests = requestReceived.incrementAndGet();
//        System.out.printf("%s queueChunkTask %s\n", new Date(), requests);
//            // TODO: Actually generate entities
//            sendBytes(chunkTask.ctx(), PackageID.NEW_ENTITY, ServerEntity.create(ServerEntity.));
//            ServerEntity.create()
//            byte[] entityID = new byte[32];
//            new SecureRandom().nextBytes(entityID);
//            sendBytes(chunkTask.ctx(), PackageID.NEW_ENTITY, new ServerEntity(entityID, chunkTask.x() * ConstantGameSettings.CHUNK_WIDTH, chunkTask.y() * ConstantGameSettings.CHUNK_HEIGHT, chunkTask.z() * ConstantGameSettings.CHUNK_LENGTH, 0, 0, 0).getBytes());
    }

    private ServerPlayer getServerPlayer(ByteBuf byteBuf) {
        StringBuilder clientID = new StringBuilder();
        byte[] clientIDBytes = getBytes(byteBuf, 4, 32);
        for (int i = 0; i < 32; i++) {
            clientID.append(String.format("%02X", clientIDBytes[i]));
        }
        return clients.get(clientID.toString());
    }

    private void registerClient(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] versionID = getBytes(byteBuf, 4, 8);
        if (Arrays.equals(versionID, String.format("%-8s", VERSION_ID).getBytes())) {
            String clientID = bytesToHex(byteBuf, 12, 32);
            ServerPlayer serverPlayer = new ServerPlayer(clientID, ctx);
            byte[] encodedServerPlayer = serverPlayer.getBytes();

            byte[] playerList = new byte[32 * clients.size()];

            final int[] i = {0};

            // Send the client all the player information
            clients.values().forEach(player -> {
                sendBytes(player.getCTX(), PackageID.NEW_PLAYER, encodedServerPlayer);
                byte[] playerBytes = player.getBytes();
                System.arraycopy(playerBytes, 0, playerList, i[0] * 32, 32);
                i[0]++;
            });
            sendBytes(ctx, PackageID.REGISTER_PLAYERS, playerList);

            clients.put(clientID, serverPlayer);

            System.out.println("Registered Client: " + clientID);
        } else {
            System.err.println("Client has different version, disconnecting...");
            System.err.println("\tClient: " + Arrays.toString(versionID));
            System.err.println("\tServer: " + Arrays.toString(String.format("%-8s", VERSION_ID).getBytes()));
            ctx.close();
        }
    }

    private String bytesToHex(ByteBuf byteBuf, int start, int length) {
        StringBuilder hex = new StringBuilder();
        byte[] clientIDBytes = getBytes(byteBuf, start, length);
        for (int i = 0; i < 32; i++) {
            hex.append(String.format("%02X", clientIDBytes[i]));
        }
        return hex.toString();
    }

    private byte[] getBytes(ByteBuf byteBuf, int i, int length) {
        byte[] bytes = new byte[length];
        byteBuf.getBytes(i, bytes);
        return bytes;
    }
}