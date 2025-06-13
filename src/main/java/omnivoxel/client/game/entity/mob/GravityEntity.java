package omnivoxel.client.game.entity.mob;

import omnivoxel.client.game.entity.Entity;
import omnivoxel.client.game.hitbox.Hitbox;

public abstract class GravityEntity extends Entity {
    protected GravityEntity(Hitbox hitbox) {
        super(hitbox);
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        // TODO: Get gravity working
//        velocityY -= ConstantGameSettings.GRAVITY;
    }
}