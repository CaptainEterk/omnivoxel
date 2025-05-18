package omnivoxel.client.game.entity.mob;

public abstract class MobEntity extends GravityEntity {
    private final String name;

    protected MobEntity(String name, float friction) {
        super(friction);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}