package omnivoxel.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.client.ServerPlayer;
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.ChunkGeneratorThread;
import omnivoxel.server.client.chunk.ChunkTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int VERSION_ID = 0;

    private final Map<String, ServerPlayer> clients;
    private final Set<BlockingQueue<ChunkTask>> chunkTasks;

    public Server(ChunkGenerator chunkGenerator) {
        this.clients = new HashMap<>();
        chunkTasks = ConcurrentHashMap.newKeySet();

        Map<ChunkPosition, byte[]> generatedChunks = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT);
        for (int i = 0; i < ConstantServerSettings.CHUNK_GENERATOR_THREAD_LIMIT; i++) {
            ChunkGeneratorThread thread = new ChunkGeneratorThread(chunkGenerator, generatedChunks);
            executorService.execute(thread);
            chunkTasks.add(thread.getChunkTasks());
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
                int x = byteBuf.getInt(36);
                int y = byteBuf.getInt(40);
                int z = byteBuf.getInt(44);
                queueChunkTask(new ChunkTask(ctx, x, y, z));
                break;
            case REGISTER_CLIENT:
                registerClient(ctx, byteBuf);
                break;
            case SET_PLAYER:
                setPlayer(byteBuf);
                break;
            case CLOSE:
                clients.remove(bytesToHex(byteBuf, 4, 32));
                break;
            default:
                System.err.println("Unknown package id: " + packageID);
        }
    }

    private void queueChunkTask(ChunkTask chunkTask) {
        try {
            BlockingQueue<ChunkTask> smallestQueue = null;
            int smallestSize = Integer.MAX_VALUE;
            for (BlockingQueue<ChunkTask> queue : chunkTasks) {
                int size = queue.size();
                if (size < smallestSize) {
                    smallestQueue = queue;
                    smallestSize = size;
                }
                if (smallestSize == 0) {
                    break;
                }
            }
            if (smallestQueue != null) {
                smallestQueue.put(chunkTask);
            }
//            // TODO: Actually generate entities
//            byte[] entityID = new byte[32];
//            new SecureRandom().nextBytes(entityID);
//            sendBytes(chunkTask.ctx(), PackageID.NEW_ENTITY, new ServerEntity(entityID, chunkTask.x() * ConstantGameSettings.CHUNK_WIDTH, chunkTask.y() * ConstantGameSettings.CHUNK_HEIGHT, chunkTask.z() * ConstantGameSettings.CHUNK_LENGTH, 0, 0, 0).getBytes());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPlayer(ByteBuf byteBuf) {
        ServerPlayer serverPlayer = getServerPlayer(byteBuf);
        int x = byteBuf.getInt(36);
        int y = byteBuf.getInt(40);
        int z = byteBuf.getInt(44);
        float pitch = byteBuf.getFloat(48);
        float yaw = byteBuf.getFloat(52);
        serverPlayer.set(x, y, z, pitch, yaw);

        byte[] encodedServerPlayer = serverPlayer.getBytes();

        // Send the client all the player information
        clients.values().forEach(player -> {
            if (player.getPlayerID() != serverPlayer.getPlayerID()) {
                sendBytes(player.getCTX(), PackageID.UPDATE_PLAYER, encodedServerPlayer);
            }
        });
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