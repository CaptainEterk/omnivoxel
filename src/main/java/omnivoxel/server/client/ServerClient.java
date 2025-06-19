package omnivoxel.server.client;

import io.netty.channel.ChannelHandlerContext;
import omnivoxel.server.entity.EntityType;
import omnivoxel.server.entity.mob.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public class ServerClient implements ServerItem {
    private final String clientID;
    private final ChannelHandlerContext ctx;
    private final byte[] playerID;
    private final PlayerEntity player;

    public ServerClient(String clientID, ChannelHandlerContext ctx) {
        this.clientID = clientID;
        this.ctx = ctx;
        playerID = new byte[32];
        new SecureRandom().nextBytes(playerID);
        player = new PlayerEntity(clientID, playerID);
    }

    public void set(float x, float y, float z, float pitch, float yaw) {
        player.set(x, y, z, pitch, yaw);
    }

    @Override
    public byte[] getBytes() {
        byte[] out = new byte[playerID.length + 24];
        System.arraycopy(playerID, 0, out, 0, playerID.length);
        addInt(out, EntityType.Type.PLAYER.ordinal(), playerID.length);
        addFloat(out, player.getX(), playerID.length + 4);
        addFloat(out, player.getY(), playerID.length + 8);
        addFloat(out, player.getZ(), playerID.length + 12);
        addFloat(out, player.getPitch(), playerID.length + 16);
        addFloat(out, player.getYaw(), playerID.length + 20);
        return out;
    }

    private void addFloat(byte @NotNull [] bytes, float f, int index) {
        int i = Float.floatToIntBits(f);
        bytes[index] = (byte) (i >> 24);
        bytes[index + 1] = (byte) (i >> 16);
        bytes[index + 2] = (byte) (i >> 8);
        bytes[index + 3] = (byte) (i);
    }

    private void addInt(byte @NotNull [] bytes, int i, int index) {
        bytes[index] = (byte) (i >> 24);
        bytes[index + 1] = (byte) (i >> 16);
        bytes[index + 2] = (byte) (i >> 8);
        bytes[index + 3] = (byte) (i);
    }

    public ChannelHandlerContext getCTX() {
        return ctx;
    }

    public byte[] getPlayerID() {
        return playerID;
    }

    public String getClientID() {
        return clientID;
    }
}