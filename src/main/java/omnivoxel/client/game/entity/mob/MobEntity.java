package omnivoxel.client.game.entity.mob;

import omnivoxel.client.game.hitbox.Hitbox;

public abstract class MobEntity extends GravityEntity {
    private final String name;

    protected MobEntity(String name, Hitbox hitbox) {
        super(hitbox);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}