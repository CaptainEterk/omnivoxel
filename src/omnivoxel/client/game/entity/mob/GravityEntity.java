package omnivoxel.client.game.entity.mob;

import omnivoxel.client.game.entity.Entity;

public abstract class GravityEntity extends Entity {
    protected GravityEntity(float friction) {
        super(friction);
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
    }
}