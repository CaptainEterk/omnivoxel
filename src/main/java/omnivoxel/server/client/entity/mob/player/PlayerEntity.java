package omnivoxel.server.client.entity.mob.player;

import omnivoxel.server.client.entity.mob.MobEntity;
import omnivoxel.client.game.hitbox.Hitbox;

import java.util.Arrays;

public class PlayerEntity extends MobEntity {
    protected final byte[] playerID;

    public PlayerEntity(String name, byte[] playerID) {
        super(name, new Hitbox(0, 0, 0, 1, 2, 1));
        this.playerID = playerID;
    }

    @Override
    public String toString() {
        return "PlayerEntity{" + "playerID=" + Arrays.toString(playerID) + '}';
    }
}