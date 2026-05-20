package omnivoxel.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.common.BlockShape;
import omnivoxel.common.block.hitbox.BlockHitbox;
import omnivoxel.common.network.NetworkService;
import omnivoxel.common.network.NetworkUser;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.common.settings.ConstantServerSettings;
import omnivoxel.common.settings.Settings;
import omnivoxel.server.client.ServerClient;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.block.ServerBlockAndPosition;
import omnivoxel.server.client.chunk.ChunkService;
import omnivoxel.server.client.chunk.ChunkTask;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.WorldGenerator;
import omnivoxel.server.entity.Entity;
import omnivoxel.server.entity.EntityManager;
import omnivoxel.server.entity.EntityStorage;
import omnivoxel.server.entity.mob.PlayerEntity;
import omnivoxel.server.games.Game;
import omnivoxel.server.io.chunk.ChunkIO;
import omnivoxel.server.world.ServerWorld;
import omnivoxel.server.world.ServerWorldHandler;
import omnivoxel.util.IndexCalculator;
import omnivoxel.util.boundingBox.WorldBoundingBox;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.game.GameParser;
import omnivoxel.util.game.nodes.ArrayGameNode;
import omnivoxel.util.game.nodes.GameNode;
import omnivoxel.util.game.nodes.ObjectGameNode;
import omnivoxel.util.log.Logger;
import omnivoxel.util.math.Position2D;
import omnivoxel.util.math.Position3D;
import omnivoxel.util.thread.WorkerThreadPool;
import omnivoxel.world.chunk2d.Chunk2D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements NetworkUser {
    private static final int HANDSHAKE_ID = 0;
    private final Map<String, ServerClient> clients;
    private final WorkerThreadPool<ChunkTask> workerThreadPool;
    private final ServerWorld world;
    private final EntityStorage entityStorage;
    private final Map<String, BlockShape> blockShapeCache;
    private final Map<String, BlockHitbox[]> blockHitboxCache;
    private final ServerBlockService blockService;
    private final ServerWorldHandler worldHandler;
    private final Settings settings;
    private final EntityManager entityManager;

    public Server(Map<String, ServerClient> clients, long seed, ServerWorld world, EntityStorage entityStorage, Map<String, BlockShape> blockShapeCache, Map<String, BlockHitbox[]> blockHitboxCache, ServerBlockService blockService, Settings settings) throws IOException {
        this.clients = clients;
        this.world = world;
        this.entityStorage = entityStorage;
        this.blockShapeCache = blockShapeCache;
        this.blockHitboxCache = blockHitboxCache;
        this.blockService = blockService;
        this.entityManager = new EntityManager(entityStorage);

        GameNode gameNode = GameParser.parseNode(Files.readString(Path.of(ConstantServerSettings.GAME_LOCATION + "main.json")), Game.checkGameNodeType(GameParser.parseNode(Files.readString(Path.of(ConstantServerSettings.GAME_LOCATION + "constants.json")), null), ArrayGameNode.class));

        if (gameNode instanceof ObjectGameNode objectGameNode) {
            WorldGenerator worldGenerator = new WorldGenerator(objectGameNode.object().get("world_generator"), blockShapeCache, blockHitboxCache, seed, blockService);
            Set<WorldBoundingBox> worldBoundingBoxes = ConcurrentHashMap.newKeySet();
            workerThreadPool = new WorkerThreadPool<>(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT, () ->
                    new ChunkService(
                            new ServerWorldDataService(
                                    blockService,
                                    worldGenerator
                            ),
                            blockService,
                            world,
                            worldBoundingBoxes
                    )::serve,
                    true
            );

            this.worldHandler = new ServerWorldHandler(world, clients, workerThreadPool, worldGenerator);
            this.settings = settings;
        } else {
            throw new IllegalArgumentException("gameNode must be an ObjectGameNode, not " + gameNode.getClass());
        }
    }

    @Override
    public void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) {
        try {
            String clientID = ByteUtils.bytesToHex(byteBuf, 8, 32);
            int index = 40;
            switch (packageID) {
                case CHUNK_REQUEST:
                    int count = byteBuf.getInt(index);
                    index += 4;
                    for (int i = 0; i < count; i++) {
                        int x = byteBuf.getInt(i * 3 * Integer.BYTES + index);
                        int y = byteBuf.getInt((i * 3 + 1) * Integer.BYTES + index);
                        int z = byteBuf.getInt((i * 3 + 2) * Integer.BYTES + index);
                        workerThreadPool.submit(new ChunkTask(clients.get(clientID), x, y, z));
                    }
                    byteBuf.release();
                    break;
                case VERSION_HANDSHAKE:
                    registerClient(ctx, byteBuf, index, clientID);
                    byteBuf.release();
                    break;
                case CLOSE:
                    ServerClient client = clients.get(clientID);
                    removeClient(client);
                    Logger.info("Client Disconnected: " + clientID + " playerID: " + ByteUtils.bytesToHex(client.getPlayerID()));
                    byteBuf.release();
                    break;
                case PLAYER_UPDATE:
                    double[] data = new double[5];
                    for (int i = 0; i < 5; i++) {
                        data[i] = byteBuf.getDouble(index + i * Double.BYTES);
                    }
                    double x = data[0];
                    double y = data[1];
                    double z = data[2];
                    double pitch = data[3];
                    double yaw = data[4];
                    ServerClient serverClient = clients.get(clientID);
                    serverClient.getPlayerEntity().set(x, y, z, pitch, yaw);

                    clients.values().forEach(player -> {
                        if (!Arrays.equals(player.getPlayerID(), serverClient.getPlayerID())) {
                            NetworkService.sendBytes(player.getCTX().channel(), PackageID.ENTITY_UPDATE, null, serverClient.getPlayerEntity().getBytes());
                        }
                    });
                    byteBuf.release();
                    break;
                case REPLACE_BLOCK:
                    int bx = byteBuf.getInt(index);
                    int by = byteBuf.getInt(index + Integer.BYTES);
                    int bz = byteBuf.getInt(index + Integer.BYTES * 2);
                    int length = byteBuf.getInt(index + Integer.BYTES * 3);
                    byte[] bytes = new byte[length];
                    byteBuf.getBytes(index + Integer.BYTES * 4, bytes);
                    StringBuilder blockID = new StringBuilder();
                    for (byte b : bytes) {
                        blockID.append((char) b);
                    }
                    Logger.info("Replacing block: " + bx + " " + by + " " + bz + " with " + blockID);
                    worldHandler.replaceBlock(bx, by, bz, blockService.getBlock(blockID.toString()), clients.get(clientID));

                    byteBuf.release();
                    break;
                default:
                    Logger.error(Logger.Priority.HIGH, "Unknown package key: " + packageID);
            }
        } catch (Exception e) {
            Logger.error(Logger.Priority.HIGH, "Error handling package " + packageID + ": " + e);
            throw e;
        }
    }

    private void removeClient(ServerClient client) {
        clients.remove(client.getClientID());
        clients.values().forEach(player -> NetworkService.sendBytes(player.getCTX().channel(), PackageID.CLOSE, null, client.getPlayerID()));
    }

    private void registerClient(ChannelHandlerContext ctx, ByteBuf byteBuf, int index, String clientID) {
        byte[] versionID = ByteUtils.getBytes(byteBuf, index, 8);
        // TODO: Replace handshake id string with a long and just use the 8 bytes from that maybe?
        if (Arrays.equals(versionID, String.format("%-8s", HANDSHAKE_ID).getBytes())) {
            ServerClient serverClient;
            Entity entity = entityManager.getEntity(entityManager.translateClientToEntity(clientID));
            if (entity instanceof PlayerEntity playerEntity) {
                Logger.debug("Client " + clientID + " is registered");
                serverClient = new ServerClient(clientID, ctx, playerEntity);
            } else if (entity == null) {
                Logger.debug("New player " + clientID + " creating state");
                PlayerEntity playerEntity = new PlayerEntity(clientID);
                Random random = new Random();
                int x = random.nextInt(-ConstantCommonSettings.CHUNK_SIZE, ConstantCommonSettings.CHUNK_SIZE - 1);
                int z = random.nextInt(-ConstantCommonSettings.CHUNK_SIZE, ConstantCommonSettings.CHUNK_SIZE - 1);
                Chunk2D<Integer> chunkHeights = world.getStoredChunkHeights(new Position2D(IndexCalculator.chunkX(x), IndexCalculator.chunkZ(z)));
                int height = chunkHeights.getBlock(IndexCalculator.localX(x), IndexCalculator.localZ(z));
                playerEntity.set(IndexCalculator.localX(x)+0.5, height+3, IndexCalculator.localZ(z)+0.5);
                serverClient = new ServerClient(clientID, ctx, playerEntity);
                long entityID =  entityManager.getNewEntityID();
                entityManager.writeClientToEntityTranslation(clientID, entityID);
                entityStorage.put(new Position3D(0, 5, 0), playerEntity, entityID);
            } else {
                return;
            }

            byte[] encodedServerPlayer = serverClient.getPlayerEntity().getBytes();

            clients.values().forEach(player -> {
                NetworkService.sendBytes(player.getCTX().channel(), PackageID.NEW_ENTITY, null, encodedServerPlayer);
                NetworkService.sendBytes(ctx.channel(), PackageID.NEW_ENTITY, null, player.getPlayerEntity().getBytes());
            });

            blockShapeCache.forEach((id, blockShape) -> NetworkService.sendBytes(serverClient.getCTX().channel(), PackageID.REGISTER_BLOCK_SHAPE, null, blockShape.getBytes()));

            blockHitboxCache.forEach((id, blockHitboxes) -> {
                        byte[] bytes = new byte[(Float.BYTES * 6 + Integer.BYTES + 2) * blockHitboxes.length + Integer.BYTES * 2 + id.length()];
                        ByteUtils.addInt(bytes, id.length(), 0);

                        System.arraycopy(id.getBytes(), 0, bytes, Integer.BYTES, id.length());

                        ByteUtils.addInt(bytes, blockHitboxes.length, Integer.BYTES + id.length());

                        int idx = id.length() + Integer.BYTES * 2;
                        for (BlockHitbox blockHitbox : blockHitboxes) {
                            byte[] hitboxBytes = blockHitbox.getBytes();
                            System.arraycopy(hitboxBytes, 0, bytes, idx, hitboxBytes.length);
                            idx += hitboxBytes.length;
                        }

                        NetworkService.sendBytes(serverClient.getCTX().channel(), PackageID.REGISTER_BLOCK_HITBOX, null, bytes);
                    }
            );

            blockService.getAllBlocks().forEach((id, serverBlock) -> {
                if (serverClient.registerBlockID(id)) {
                    ChannelHandlerContext ctx1 = serverClient.getCTX();
                    NetworkService.sendBytes(ctx1.channel(), PackageID.REGISTER_BLOCK, null, serverBlock.getBytes());
                }
            });

            clients.put(clientID, serverClient);

            NetworkService.sendDoubles(ctx.channel(), PackageID.PLAYER_STATE, null, serverClient.getPlayerEntity().getX(), serverClient.getPlayerEntity().getY(), serverClient.getPlayerEntity().getZ(), serverClient.getPlayerEntity().getPitch(), serverClient.getPlayerEntity().getYaw());

            Logger.debug("Client Connected: " + clientID + " playerID: " + ByteUtils.bytesToHex(serverClient.getPlayerID()));
        } else {
            Logger.error(Logger.Priority.HIGH, "Client has an incompatible version, disconnecting...");
            Logger.error(Logger.Priority.HIGH, "\tClient: " + Arrays.toString(versionID));
            Logger.error(Logger.Priority.HIGH, "\tServer: " + Arrays.toString(String.format("%-8s", HANDSHAKE_ID).getBytes()));
            ctx.close();
        }
    }

    public void run() {
        final long tickIntervalNanos = 1_000_000_000L / settings.getIntSetting("tps", 20);

        int tick = 0;
        while (true) {
            long startNano = System.nanoTime();

            if (tick % settings.getIntSetting("lost_client_td", 10) == 0) {
                for (ServerClient client : clients.values()) {
                    if (!NetworkService.checkChannel(client.getCTX().channel())) {
                        removeClient(client);
                        Logger.info("Client Lost Contact... Disconnecting: " + client.getClientID());
                    }
                }
            }
            if (tick % settings.getIntSetting("entity_save_td", 20) == 0) {

            }

            for (int i = 0; i < 10; i++) {
                worldHandler.replaceBlock((int) Math.floor(Math.random() * 16), (int) Math.floor(Math.random() * 8) + 100, (int) Math.floor(Math.random() * 16), ServerBlock.AIR, null);
            }

            world.tick();

            sendQueuedClientPackets();

            long elapsed = System.nanoTime() - startNano;

            long sleepNanos = tickIntervalNanos - elapsed;

            tick++;

            if (sleepNanos > 0) {
                try {
                    long sleepMillis = sleepNanos / 1_000_000;
                    int sleepSubNanos = (int) (sleepNanos % 1_000_000);
                    Thread.sleep(sleepMillis, sleepSubNanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                Logger.warn(Logger.Priority.NORMAL, "Tick took too long: " + (elapsed / 1_000_000.0) + " ms");
            }
        }
    }

    private void sendQueuedClientPackets() {
        try {
            for (Map.Entry<String, ServerClient> entry : clients.entrySet()) {
                String id = entry.getKey();
                ServerClient serverClient = entry.getValue();
                Queue<ServerBlockAndPosition> queuedReplacedBlocks = serverClient.getReplacedBlocks();

                int size = queuedReplacedBlocks.size();
                byte[][] outBytes = new byte[size][];
                int byteCount = 4;
                for (int i = 0; i < size; i++) {
                    ServerBlockAndPosition block = queuedReplacedBlocks.poll();
                    if (block == null) {
                        Logger.warn(Logger.Priority.NORMAL, "Block was null when polling from queue, this should not happen");
                        break;
                    }
                    byte[] blockBytes = block.serverBlock().getBlockBytes();
                    byte[] out = new byte[16 + blockBytes.length];

                    int chunkX = Math.floorDiv(block.x(), ConstantCommonSettings.CHUNK_WIDTH);
                    int chunkZ = Math.floorDiv(block.z(), ConstantCommonSettings.CHUNK_LENGTH);

                    int x = Math.floorMod(block.x(), ConstantCommonSettings.CHUNK_WIDTH);
                    int z = Math.floorMod(block.z(), ConstantCommonSettings.CHUNK_LENGTH);

                    ByteUtils.addInt(out, block.x(), 0);
                    ByteUtils.addInt(out, block.y(), 4);
                    ByteUtils.addInt(out, block.z(), 8);
                    Position2D position2D = new Position2D(chunkX, chunkZ);
                    Chunk2D<Integer> chunk2D = world.getChunkHeights(position2D);
                    if (chunk2D == null) {
                        chunk2D = ChunkIO.decodeChunk2D(ChunkIO.getChunk2D(position2D));
                    }
                    int highestY = chunk2D.getBlock(x, z);
                    ByteUtils.addInt(out, highestY, 12);
                    System.arraycopy(blockBytes, 0, out, 16, blockBytes.length);
                    outBytes[i] = out;
                    byteCount += out.length;
                }

                byte[] out = new byte[byteCount];
                ByteUtils.addInt(out, size, 0);

                int index = 4;
                for (int i = 0; i < size; i++) {
                    System.arraycopy(outBytes[i], 0, out, index, outBytes[i].length);
                    index += outBytes[i].length;
                }

                NetworkService.sendBytes(serverClient.getCTX().channel(), PackageID.REPLACE_BLOCK, null, out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
