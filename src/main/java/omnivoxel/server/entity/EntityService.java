package omnivoxel.server.entity;

import omnivoxel.server.games.Game;
import omnivoxel.server.games.GameResources;
import omnivoxel.util.game.nodes.ArrayGameNode;
import omnivoxel.util.game.nodes.GameNode;
import omnivoxel.util.game.nodes.ObjectGameNode;
import omnivoxel.util.game.nodes.StringGameNode;

public class EntityService {
    private final GameResources gameResources;
    private final EntityDefinition[] entityDefinitions;

    public EntityService(GameNode entitiesGameNode, GameResources gameResources) {
        this.gameResources = gameResources;
        this.entityDefinitions = parseEntities(entitiesGameNode);
    }

    private EntityDefinition[] parseEntities(GameNode entitiesGameNode) {
        if (entitiesGameNode instanceof ArrayGameNode arrayGameNode) {
            EntityDefinition[] entityDefinitions = new EntityDefinition[arrayGameNode.nodes().length];

            GameNode[] nodes = arrayGameNode.nodes();
            for (int i = 0; i < nodes.length; i++) {
                entityDefinitions[i] = parseEntity(nodes[i]);
            }
            return entityDefinitions;
        } else {
            throw new IllegalStateException("\"entities\" must be an array");
        }
    }

    private EntityDefinition parseEntity(GameNode entityNode) {
        if (entityNode instanceof ObjectGameNode objectGameNode) {
            StringGameNode idNode = Game.checkGameNodeType(objectGameNode.object().get("id"), StringGameNode.class);
            if (idNode == null) {
                throw new IllegalArgumentException("Entities must have an \"id\" attribute");
            }
            String id = idNode.value();
            StringGameNode meshIDNode = Game.checkGameNodeType(objectGameNode.object().get("mesh"), StringGameNode.class);
            if (meshIDNode == null) {
                throw new IllegalArgumentException("Entities must have an \"mesh\" attribute");
            }
            String meshID = meshIDNode.value();
            ServerEntityMesh serverEntityMesh = gameResources.getEntityMesh(meshID);
            if (serverEntityMesh == null) {
                throw new IllegalArgumentException("Unknown entity mesh \"" + meshID + "\"");
            }
            return new EntityDefinition(id, serverEntityMesh);
        } else {
            throw new IllegalArgumentException("Entities must be an object");
        }
    }
}