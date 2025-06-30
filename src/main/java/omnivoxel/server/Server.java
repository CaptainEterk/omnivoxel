package omnivoxel.server;

import core.biomes.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.server.client.ServerClient;
import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.ChunkTask;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;
import omnivoxel.util.boundingBox.WorldBoundingBox;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.math.Position3D;
import omnivoxel.util.thread.WorkerThreadPool;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final int VERSION_ID = 0;

    private final Map<String, ServerClient> clients;
    private final AtomicLong requestReceived = new AtomicLong();
    private final AtomicLong requestSent = new AtomicLong();

    private final WorkerThreadPool<ChunkTask> workerThreadPool;

    public Server(long seed, ServerWorld world, ServerBlockService blockService) throws IOException {
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

        Map<Position3D, PriorityServerBlock> queuedBlocks = new ConcurrentHashMap<>();

        BiomeService biomeService = new BiomeService(
                Map.of(
//                        new ClimateVector(0.0, 0.0, 0.7, 0.3, 0.0, 0.0),
//                        new DesertBiome(blockService),
//                        new ClimateVector(0.0, 0.0, 0.7, 0.7, 0.0, 0.0),
//                        new JungleBiome(blockService),
//                        new ClimateVector(0.0, 0.0, 0.3, 0.3, 0.0, 0.0),
//                        new TundraBiome(blockService),
//                        new ClimateVector(0.0, 0.0, 0.3, 0.7, 0.0, 0.0),
//                        new TaigaBiome(blockService),
//                        new ClimateVector(0.0, 0.0, 0.5, 0.5, 0.0, 0.0),
//                        new PlainsBiome(blockService),
                        new ClimateVector(0.0, 0.0, 0.4, 0.5, 0.0, 0.0),
                        new ForestBiome(blockService),
                        new ClimateVector(0.0, 0.0, 0.0, 0.0, 0.0, 0.5),
                        new TempCaveBiome(blockService)
                )
        );
        Set<WorldBoundingBox> worldBoundingBoxes = ConcurrentHashMap.newKeySet();
        workerThreadPool = new WorkerThreadPool<>(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT, new ChunkGenerator(new BasicWorldDataService(new Random(seed), world, biomeService, blockService, queuedBlocks), blockService, biomeService, world, worldBoundingBoxes)::generateChunk);
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
        String clientID = ByteUtils.bytesToHex(byteBuf, 4, 32);
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
                ServerClient client = clients.get(clientID);
                clients.remove(clientID);
                clients.values().forEach(player -> sendBytes(player.getCTX(), PackageID.CLOSE, client.getPlayerID()));
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
                ServerClient serverClient = clients.get(clientID);
                serverClient.set(x, y, z, pitch, yaw);

                clients.values().forEach(player -> {
                    if (!Arrays.equals(player.getPlayerID(), serverClient.getPlayerID())) {
                        sendBytes(player.getCTX(), PackageID.ENTITY_UPDATE, serverClient.getBytes());
                    }
                });
                break;
            default:
                System.err.println("Unknown package id: " + packageID);
        }
    }

    private void queueChunkTask(ChunkTask chunkTask) throws InterruptedException {
        workerThreadPool.submit(chunkTask);
    }

    private void registerClient(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] versionID = ByteUtils.getBytes(byteBuf, 4, 8);
        if (Arrays.equals(versionID, String.format("%-8s", VERSION_ID).getBytes())) {
            String clientID = ByteUtils.bytesToHex(byteBuf, 12, 32);
            ServerClient serverClient = new ServerClient(clientID, ctx);
            byte[] encodedServerPlayer = serverClient.getBytes();

            byte[] playerList = new byte[32 * clients.size()];

            final int[] i = {0};

            clients.values().forEach(player -> {
                sendBytes(player.getCTX(), PackageID.NEW_PLAYER, encodedServerPlayer);
                byte[] playerBytes = player.getBytes();
                System.arraycopy(playerBytes, 0, playerList, i[0] * 32, 32);
                i[0]++;
            });
            sendBytes(ctx, PackageID.REGISTER_PLAYERS, playerList);

            clients.put(clientID, serverClient);

            System.out.println("Registered Client: " + clientID + " with playerID: " + ByteUtils.bytesToHex(serverClient.getPlayerID()));
        } else {
            System.err.println("Client has different version, disconnecting...");
            System.err.println("\tClient: " + Arrays.toString(versionID));
            System.err.println("\tServer: " + Arrays.toString(String.format("%-8s", VERSION_ID).getBytes()));
            ctx.close();
        }
    }
}