package omnivoxel.server.entity.mob;

import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.entity.Entity;

public abstract class GravityEntity extends Entity {
    protected GravityEntity(String entityID, String meshID, Hitbox hitbox) {
        super(entityID, meshID, hitbox);
    }
}