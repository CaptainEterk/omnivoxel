package omnivoxel.client.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import omnivoxel.client.game.entity.Entity;
import omnivoxel.client.game.entity.mob.player.PlayerEntity;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.thread.mesh.MeshDataGenerator;
import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.BlockStateWrapper;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.network.block.ClientBlock;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.client.network.request.CloseRequest;
import omnivoxel.client.network.request.Request;
import omnivoxel.debug.Logger;
import omnivoxel.server.PackageID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class Client {
    private final Map<String, PlayerEntity> players;
    private final byte[] clientID;
    private final ClientWorldDataService worldDataService;
    private final Logger logger;
    private final MeshDataGenerator meshDataGenerator;
    private EventLoopGroup group;
    private Channel channel;
    private BiConsumer<ChunkPosition, MeshData> loadChunk;
    private BiConsumer<String, Entity> loadEntity;

    public Client(byte[] clientID, ClientWorldDataService worldDataService, Logger logger) {
        this.clientID = clientID;
        this.worldDataService = worldDataService;
        this.logger = logger;
        players = new ConcurrentHashMap<>();
        meshDataGenerator = new MeshDataGenerator(logger);
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

    private void flush(Channel channel, ByteBuf byteBuf) {
        channel.writeAndFlush(byteBuf).addListener(f -> {
            if (!f.isSuccess()) {
                System.err.println("[ERROR] Failed: " + f.cause());
                f.cause().printStackTrace();
            }
        });
    }

    void handlePackage(ChannelHandlerContext ctx, PackageID packageID, ByteBuf byteBuf) {
        switch (packageID) {
            case REGISTER_PLAYERS:
                registerPlayers(byteBuf);
                break;
            case NEW_PLAYER:
                newPlayer(byteBuf);
                break;
            case CHUNK:
                receiveChunk(byteBuf);
                break;
            case PLAYER_UPDATE:
                updatePlayer(byteBuf);
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

    private void receiveChunk(ByteBuf byteBuf) {
        int x = byteBuf.getInt(8);
        int y = byteBuf.getInt(12);
        int z = byteBuf.getInt(16);
        ChunkPosition chunkPosition = new ChunkPosition(x, y, z);

        ClientBlock[] palette = new ClientBlock[byteBuf.getShort(20)];
        int index = 22;
        for (int i = 0; i < palette.length; i++) {
            StringBuilder blockID = new StringBuilder();
            short paletteLength = byteBuf.getShort(index);
            short j;
            for (j = 2; j < paletteLength + 2; j++) {
                byte b = byteBuf.getByte(index + j);
                blockID.append((char) b);
            }
            short blockStateCount = byteBuf.getShort(index + j);
            j += 2;
            int[] blockState = new int[blockStateCount];
            for (int k = 0; k < blockStateCount; k++) {
                blockState[k] = (int) byteBuf.getUnsignedInt(index + j);
                j += 4;
            }
            palette[i] = new ClientBlock(blockID.toString(), blockState);
            index += j;
        }

        Block[] blocks = new Block[ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED];
        for (int i = 0; i < ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED && index < byteBuf.readableBytes(); ) {
            int blockID = byteBuf.getInt(index);
            int blockCount = byteBuf.getInt(index + 4);
            if (blockID == 0) {
                i += blockCount;
            } else {
                int oi = i;
                for (; i < blockCount + oi && i < ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED; i++) {
                    blocks[i] = worldDataService.getBlock(palette[blockID - 1].id());
                    if (palette[blockID - 1].blockState().length > 0) {
                        blocks[i] = new BlockStateWrapper(blocks[i], palette[blockID - 1].blockState());
                    }
                }
            }
            index += 8;
        }

        MeshData meshData = meshDataGenerator.generateChunkMeshData(blocks);
        loadChunk.accept(chunkPosition, meshData);
    }

    private void loadPlayer(byte[] playerID, String name) {
        PlayerEntity playerEntity = new PlayerEntity(name, playerID);
        String id = bytesToHex(playerID);
        players.put(id, playerEntity);
        // Generate mesh data
        MeshData meshData = meshDataGenerator.generateEntityMeshData(playerEntity);
        playerEntity.setMeshData(meshData);
        //
        loadEntity.accept(id, playerEntity);
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

    public void sendRequest(Request request) {
        switch (request.getType()) {
            case CHUNK:
                ChunkPosition chunkPosition = ((ChunkRequest) request).chunkPosition();
                sendInts(channel, PackageID.CHUNK_REQUEST, clientID, chunkPosition.x(), chunkPosition.y(), chunkPosition.z());
                break;
            case CLOSE:
                sendBytes(channel, PackageID.CLOSE, clientID);
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
        }
        System.out.println("Client shutdown");
    }

    public void setChunkListener(BiConsumer<ChunkPosition, MeshData> loadChunk) {
        this.loadChunk = loadChunk;
    }

    public void setEntityListener(BiConsumer<String, Entity> loadEntity) {
        this.loadEntity = loadEntity;
    }
}