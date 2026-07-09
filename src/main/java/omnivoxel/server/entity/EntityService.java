package omnivoxel.server.entity;

import omnivoxel.common.resource.GameResources;
import omnivoxel.util.game.nodes.GameNode;

public class EntityService {
    private final GameResources resources;

    public EntityService(GameNode entitiesGameNode, GameResources resources) {
        this.resources = resources;
    }
}