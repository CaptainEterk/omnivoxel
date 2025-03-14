package omnivoxel.server.client;

import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.position.ChangingPosition;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public class ServerPlayer implements ServerItem {
    private final String clientID;
    private final ChannelHandlerContext ctx;
    private final byte[] playerID;
    private final ChangingPosition position;
    private float pitch;
    private float yaw;

    public ServerPlayer(String clientID, ChannelHandlerContext ctx) {
        this.clientID = clientID;
        this.ctx = ctx;
        playerID = new byte[32];
        new SecureRandom().nextBytes(playerID);
        position = new ChangingPosition(0, 0, 0);
    }

    public void set(int x, int y, int z, float pitch, float yaw) {
        this.position.setX(x);
        this.position.setY(y);
        this.position.setZ(z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public byte[] getBytes() {
        byte[] out = new byte[playerID.length + 20];
        System.arraycopy(playerID, 0, out, 0, playerID.length);
        addFloat(out, position.x(), playerID.length);
        addFloat(out, position.y(), playerID.length + 4);
        addFloat(out, position.z(), playerID.length + 8);
        addFloat(out, pitch, playerID.length + 12);
        addFloat(out, yaw, playerID.length + 16);
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

    @Override
    public String toString() {
        return "ServerPlayer{" +
                "id='" + clientID + '\'' +
                ", position=" + position +
                '}';
    }
}