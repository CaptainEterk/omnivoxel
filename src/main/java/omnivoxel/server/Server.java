package omnivoxel.server;

import core.biomes.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.math.Position3D;
import omnivoxel.server.client.ServerPlayer;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.ChunkGeneratorThread;
import omnivoxel.server.client.chunk.ChunkTask;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final int VERSION_ID = 0;

    private final Map<String, ServerPlayer> clients;
    private final BlockingQueue<ChunkTask> chunkTasks;
    private final AtomicLong requestReceived = new AtomicLong();
    private final AtomicLong requestSent = new AtomicLong();

    public Server(long seed, ServerWorld world) throws IOException {
        this.clients = new ConcurrentHashMap<>();
        chunkTasks = new LinkedBlockingDeque<>();

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

        BiomeService biomeService = new BiomeService(
                Map.of(
                        new ClimateVector(0.0, 0.0, 0.7, 0.3, 0.0),
                        new DesertBiome(blockService),
                        new ClimateVector(0.0, 0.0, 0.7, 0.7, 0.0),
                        new JungleBiome(blockService),
                        new ClimateVector(0.0, 0.0, 0.3, 0.3, 0.0),
                        new TundraBiome(blockService),
                        new ClimateVector(0.0, 0.0, 0.3, 0.7, 0.0),
                        new TaigaBiome(blockService),
                        new ClimateVector(0.0, 0.0, 0.5, 0.5, 0.0),
                        new PlainsBiome(blockService)
                )
        );
        ExecutorService executorService = Executors.newFixedThreadPool(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT);
        for (int i = 0; i < ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT; i++) {
            Random random = new Random(seed);
            ChunkGenerator chunkGenerator = new ChunkGenerator(new BasicWorldDataService(random, world, biomeService, blockService, queuedBlocks), blockService, biomeService);
            ChunkGeneratorThread thread = new ChunkGeneratorThread(chunkGenerator, chunkTasks, world);
            executorService.execute(thread);
        }
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

    public void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) {
        switch (packageID) {
            case CHUNK_REQUEST:
                int count = byteBuf.getInt(36);
                for (int i = 0; i < count; i++) {
                    int x = byteBuf.getInt(i * 3 * Integer.BYTES + 40);
                    int y = byteBuf.getInt(i * 3 * Integer.BYTES + 44);
                    int z = byteBuf.getInt(i * 3 * Integer.BYTES + 48);
                    queueChunkTask(new ChunkTask(ctx, x, y, z));
                }
                break;
            case REGISTER_CLIENT:
                registerClient(ctx, byteBuf);
                break;
            case CLOSE:
                clients.remove(bytesToHex(byteBuf, 4, 32));
                break;
            default:
                System.err.println("Unknown package id: " + packageID);
        }
    }

    private void queueChunkTask(ChunkTask chunkTask) {
        chunkTasks.add(chunkTask);
        long requests = requestReceived.incrementAndGet();
        System.out.printf("%s queueChunkTask %s\n", new Date(), requests);
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