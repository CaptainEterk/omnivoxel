package omnivoxel.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.common.BlockShape;
import omnivoxel.server.client.ServerClient;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.ChunkTask;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.util.boundingBox.WorldBoundingBox;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.game.GameParser;
import omnivoxel.util.game.nodes.GameNode;
import omnivoxel.util.game.nodes.ObjectGameNode;
import omnivoxel.util.thread.WorkerThreadPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int HANDSHAKE_ID = 0;
    private static final int TPS = 20;
    private final Map<String, ServerClient> clients;
    private final WorkerThreadPool<ChunkTask> workerThreadPool;
    private final ServerWorld world;
    private final Map<String, String> blockIDMap;
    private final Map<String, BlockShape> blockShapeCache;
    private final ServerBlockService blockService;

    public Server(int seed, ServerWorld world, Map<String, BlockShape> blockShapeCache, ServerBlockService blockService, Map<String, String> blockIDMap) throws InterruptedException, IOException {
        this.world = world;
        this.blockShapeCache = blockShapeCache;
        this.blockIDMap = blockIDMap;
        this.blockService = blockService;
        this.clients = new ConcurrentHashMap<>();

        GameNode gameNode = GameParser.parseNode(Files.readString(Path.of("game/main.json")));

        if (gameNode instanceof ObjectGameNode objectGameNode) {
            ServerWorldDataService serverWorldDataService = new ServerWorldDataService(blockService, objectGameNode.object().get("world_generator"));
            Set<WorldBoundingBox> worldBoundingBoxes = ConcurrentHashMap.newKeySet();
            workerThreadPool = new WorkerThreadPool<>(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT, new ChunkGenerator(serverWorldDataService, blockService, world, worldBoundingBoxes)::generateChunk, true);
        } else {
            throw new IllegalArgumentException("gameNode must be an ObjectGameNode, not " + gameNode.getClass());
        }
    }

    // TODO: Cleanup the server

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

    private static void sendBlock(ChannelHandlerContext ctx, ServerBlock block) {
        ByteBuf buffer = Unpooled.buffer();
        byte[] bytes = block.getBytes();
        buffer.writeInt(4 + bytes.length);
        buffer.writeInt(PackageID.REGISTER_BLOCK.ordinal());
        buffer.writeBytes(bytes);
        ctx.channel().writeAndFlush(buffer);
    }

    private static void sendBlockShape(ChannelHandlerContext ctx, BlockShape blockShape) {
        ByteBuf buffer = Unpooled.buffer();
        byte[] bytes = blockShape.getBytes();
        buffer.writeInt(4 + bytes.length);
        buffer.writeInt(PackageID.REGISTER_BLOCK_SHAPE.ordinal());
        buffer.writeBytes(bytes);
        ctx.channel().writeAndFlush(buffer);
    }

    public void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) throws
            InterruptedException {
        String clientID = ByteUtils.bytesToHex(byteBuf, 4, 32);
        switch (packageID) {
            case CHUNK_REQUEST:
                int count = byteBuf.getInt(36);
                for (int i = 0; i < count; i++) {
                    int x = byteBuf.getInt(i * 3 * Integer.BYTES + 40);
                    int y = byteBuf.getInt(i * 3 * Integer.BYTES + 44);
                    int z = byteBuf.getInt(i * 3 * Integer.BYTES + 48);
                    queueChunkTask(new ChunkTask(clients.get(clientID), x, y, z, byteBuf));
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
                double[] data = new double[5];
                for (int i = 0; i < 5; i++) {
                    data[i] = byteBuf.getDouble(36 + i * Double.BYTES);
                }
                double x = data[0];
                double y = data[1];
                double z = data[2];
                double pitch = data[3];
                double yaw = data[4];
                ServerClient serverClient = clients.get(clientID);
                serverClient.set(x, y, z, pitch, yaw);

                clients.values().forEach(player -> {
                    if (!Arrays.equals(player.getPlayerID(), serverClient.getPlayerID())) {
                        sendBytes(player.getCTX(), PackageID.ENTITY_UPDATE, serverClient.getBytes());
                    }
                });
                break;
            default:
                System.err.println("Unknown package key: " + packageID);
        }
    }

    private void queueChunkTask(ChunkTask chunkTask) throws InterruptedException {
        workerThreadPool.submit(chunkTask);
    }

    private void registerClient(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] versionID = ByteUtils.getBytes(byteBuf, 4, 8);
        if (Arrays.equals(versionID, String.format("%-8s", HANDSHAKE_ID).getBytes())) {
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

            blockShapeCache.forEach((id, blockShape) -> sendBlockShape(serverClient.getCTX(), blockShape));

            blockService.getAllBlocks().forEach((id, serverBlock) -> {
                if (serverClient.registerBlockID(id)) {
                    sendBlock(serverClient.getCTX(), serverBlock);
                }
            });

            clients.put(clientID, serverClient);

            ServerLogger.logger.debug("Registered Client: " + clientID + " with playerID: " + ByteUtils.bytesToHex(serverClient.getPlayerID()));
        } else {
            System.err.println("Client has different version, disconnecting...");
            System.err.println("\tClient: " + Arrays.toString(versionID));
            System.err.println("\tServer: " + Arrays.toString(String.format("%-8s", HANDSHAKE_ID).getBytes()));
            ctx.close();
        }
    }

    public void run() {
        try {
            final long tickIntervalNanos = 1_000_000_000L / TPS;

            int tick = 0;
            while (true) {
                long startNano = System.nanoTime();

                world.tick();

                long elapsed = System.nanoTime() - startNano;
                long sleepNanos = tickIntervalNanos - elapsed;

                if (sleepNanos > 0) {
                    try {
                        long sleepMillis = sleepNanos / 1_000_000;
                        int sleepSubNanos = (int) (sleepNanos % 1_000_000);
                        Thread.sleep(sleepMillis, sleepSubNanos);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // reset interrupted flag
                        break;
                    }
                } else {
                    // Tick overran — consider logging or skipping sleep
                    System.err.println("Tick took too long: " + (elapsed / 1_000_000.0) + " ms");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}