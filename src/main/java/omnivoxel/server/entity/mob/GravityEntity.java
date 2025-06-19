package omnivoxel.server.entity.mob;

import omnivoxel.server.entity.Entity;
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