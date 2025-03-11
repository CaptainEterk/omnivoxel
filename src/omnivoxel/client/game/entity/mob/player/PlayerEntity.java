package omnivoxel.client.game.entity.mob.player;

import omnivoxel.client.game.entity.mob.MobEntity;

import java.util.Arrays;

public class PlayerEntity extends MobEntity {
    protected final byte[] playerID;

    public PlayerEntity(String name, byte[] playerID) {
        super(name, 0f);
        this.playerID = playerID;
    }

    @Override
    public String toString() {
        return "PlayerEntity{" +
                "playerID=" + Arrays.toString(playerID) +
                '}';
    }
}