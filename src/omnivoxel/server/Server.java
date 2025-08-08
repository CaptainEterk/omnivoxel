package omnivoxel.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.ServerClient;
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.ChunkTask;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.WorldGenAPI;
import omnivoxel.server.games.GameAPI;
import omnivoxel.server.games.GameFileSystem;
import omnivoxel.server.games.GameFinder;
import omnivoxel.util.boundingBox.WorldBoundingBox;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.config.Config;
import omnivoxel.util.log.Logger;
import omnivoxel.util.thread.WorkerThreadPool;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int HANDSHAKE_ID = 0;
    private static final int TPS = 20;
    private final Map<String, ServerClient> clients;
    private final WorkerThreadPool<ChunkTask> workerThreadPool;
    private final Value script;
    private final Logger logger;

    public Server(int seed, ServerWorld world, ServerBlockService blockService, Logger logger) throws InterruptedException {
        this.logger = logger;
        this.clients = new ConcurrentHashMap<>();

        File gamesDir = new File(ConstantGameSettings.GAME_LOCATION);

        GameFinder gameFinder = new GameFinder(gamesDir);
        List<String> games = gameFinder.findGames();

        if (games.isEmpty()) {
            throw new InterruptedException("No valid games found.");
        }

        String selected = games.getFirst();
        this.script = launchGame(selected, seed);

        Value worldGenerator = this.script.invokeMember("worldGenerator");

        ServerWorldDataService serverWorldDataService = new ServerWorldDataService(blockService, worldGenerator);

        Set<WorldBoundingBox> worldBoundingBoxes = ConcurrentHashMap.newKeySet();
        workerThreadPool = new WorkerThreadPool<>(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT, new ChunkGenerator(serverWorldDataService, blockService, world, worldBoundingBoxes)::generateChunk, true);
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

    private Value launchGame(String gameDirectory, int seed) {
        Config config = new Config(gameDirectory + "/game.properties");
        GameAPI.GAME_DIRECTORY = gameDirectory;
        String scriptPath = config.get("path");

        if (scriptPath == null || scriptPath.isBlank()) {
            throw new IllegalArgumentException("Missing 'path' in game.properties");
        }

        File jsFile = new File(gameDirectory, scriptPath);
        if (!jsFile.exists()) {
            throw new IllegalArgumentException("JS script not found: " + jsFile.getAbsolutePath());
        }
        try {
            HostAccess hostAccess = HostAccess.newBuilder()
                    .allowPublicAccess(false)
                    .allowAccessAnnotatedBy(HostAccess.Export.class)
                    .build();

            Context ctx = Context.newBuilder("js")
                    .allowHostAccess(hostAccess)
                    .allowHostClassLookup(className -> false)
                    .allowIO(true)
                    .fileSystem(new GameFileSystem(Path.of(gameDirectory)))
                    .build();

            // TODO: Maybe add settings, state, etc...
            ctx.getBindings("js").putMember("game", new GameAPI());
            ctx.getBindings("js").putMember("worldGen", new WorldGenAPI(seed));

            Value script = ctx.eval(Source.newBuilder("js", jsFile).build());

            if (script.hasMember("init")) {
                script.getMember("init").execute();
            }

            return script;
        } catch (Exception e) {
            throw new RuntimeException("Game failed to launch", e);
        }
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
                    queueChunkTask(new ChunkTask(clients.get(clientID), x, y, z));
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
                System.err.println("Unknown package id: " + packageID);
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

            clients.put(clientID, serverClient);

            logger.debug("Registered Client: " + clientID + " with playerID: " + ByteUtils.bytesToHex(serverClient.getPlayerID()));
        } else {
            System.err.println("Client has different version, disconnecting...");
            System.err.println("\tClient: " + Arrays.toString(versionID));
            System.err.println("\tServer: " + Arrays.toString(String.format("%-8s", HANDSHAKE_ID).getBytes()));
            ctx.close();
        }
    }

    public void run() {
        final long tickIntervalNanos = 1_000_000_000L / TPS;

        int tick = 0;
        while (true) {
            long startNano = System.nanoTime();

            if (script.hasMember("tick")) {
                try {
                    script.invokeMember("tick", tick++);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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
    }

    public void stop() {
        script.getContext().close();
    }
}