package omnivoxel.server.client;

import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.entity.EntityType;
import omnivoxel.server.entity.mob.MobEntity;
import omnivoxel.util.bytes.ByteUtils;

import java.security.SecureRandom;

public class ServerClient extends MobEntity implements ServerItem {
    private final String clientID;
    private final ChannelHandlerContext ctx;
    private final byte[] playerID;

    public ServerClient(String clientID, ChannelHandlerContext ctx) {
        super(clientID, new Hitbox(0, 0, 0, 1, 2, 1, 2, 3, 2));
        this.clientID = clientID;
        this.ctx = ctx;
        playerID = new byte[32];
        new SecureRandom().nextBytes(playerID);
    }

    @Override
    public byte[] getBytes() {
        byte[] out = new byte[playerID.length + 24];
        System.arraycopy(playerID, 0, out, 0, playerID.length);
        ByteUtils.addInt(out, EntityType.Type.PLAYER.ordinal(), playerID.length);
        ByteUtils.addFloat(out, x, playerID.length + 4);
        ByteUtils.addFloat(out, y, playerID.length + 8);
        ByteUtils.addFloat(out, z, playerID.length + 12);
        ByteUtils.addFloat(out, pitch, playerID.length + 16);
        ByteUtils.addFloat(out, yaw, playerID.length + 20);
        return out;
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