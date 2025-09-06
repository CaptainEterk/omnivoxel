package omnivoxel.server.client;

import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.entity.EntityType;
import omnivoxel.server.entity.mob.MobEntity;
import omnivoxel.util.bytes.ByteUtils;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class ServerClient extends MobEntity implements ServerItem {
    private final String clientID;
    private final ChannelHandlerContext ctx;
    private final byte[] playerID;
    public final Set<String> registeredIDs;

    public ServerClient(String clientID, ChannelHandlerContext ctx) {
        super(clientID, new Hitbox(0, 0, 0, 1, 2, 1, 2, 3, 2));
        this.clientID = clientID;
        this.ctx = ctx;
        playerID = new byte[32];
        new SecureRandom().nextBytes(playerID);
        registeredIDs = new HashSet<>();
    }

    @Override
    public byte[] getBytes() {
        byte[] out = new byte[playerID.length + Integer.BYTES + Double.BYTES * 5];
        System.arraycopy(playerID, 0, out, 0, playerID.length);
        ByteUtils.addInt(out, EntityType.Type.PLAYER.ordinal(), playerID.length);
        ByteUtils.addDouble(out, x, playerID.length + Integer.BYTES);
        ByteUtils.addDouble(out, y, playerID.length + Integer.BYTES + Double.BYTES);
        ByteUtils.addDouble(out, z, playerID.length + Integer.BYTES + Double.BYTES * 2);
        ByteUtils.addDouble(out, pitch, playerID.length + Integer.BYTES + Double.BYTES * 3);
        ByteUtils.addDouble(out, yaw, playerID.length + Integer.BYTES + Double.BYTES * 4);
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

    public boolean registerBlockID(String id) {
        return registeredIDs.add(id);
    }
}