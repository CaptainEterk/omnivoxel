package omnivoxel.server.client;

import io.netty.channel.ChannelHandlerContext;
import omnivoxel.server.client.entity.mob.player.PlayerEntity;
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
        byte[] out = new byte[playerID.length + 20];
        System.arraycopy(playerID, 0, out, 0, playerID.length);
        addFloat(out, player.getX(), playerID.length);
        addFloat(out, player.getY(), playerID.length + 4);
        addFloat(out, player.getZ(), playerID.length + 8);
        addFloat(out, player.getPitch(), playerID.length + 12);
        addFloat(out, player.getYaw(), playerID.length + 16);
        return out;
    }

    private void addFloat(byte @NotNull [] bytes, float f, int index) {
        int i = Float.floatToIntBits(f);
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