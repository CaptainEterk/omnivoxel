package omnivoxel.server.games;

import omnivoxel.common.entity.EntityVertex;
import omnivoxel.server.entity.ServerEntityMesh;
import omnivoxel.server.entity.ServerEntityShape;
import omnivoxel.server.entity.ServerEntityTexture;
import omnivoxel.util.game.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class GameResources {
    private final Map<String, ServerEntityShape> serverEntityShapes;
    private final Map<String, ServerEntityTexture> serverEntityTextures;
    private final Map<String, ServerEntityMesh> serverEntityMeshes;

    public GameResources(ObjectGameNode gameNode) {
        ArrayGameNode entityShapesNode = Game.checkGameNodeType(gameNode.object().get("entity_shapes"), ArrayGameNode.class);
        if (entityShapesNode == null) {
            throw new NullPointerException("Resources must include \"entity_shapes\" attribute");
        }
        serverEntityShapes = loadServerEntityShapes(entityShapesNode);
        ArrayGameNode entityTexturesNode = Game.checkGameNodeType(gameNode.object().get("entity_textures"), ArrayGameNode.class);
        if (entityTexturesNode == null) {
            throw new NullPointerException("Resources must include \"entity_textures\" attribute");
        }
        serverEntityTextures = loadServerEntityTextures(entityTexturesNode);
        ArrayGameNode entityMeshesNode = Game.checkGameNodeType(gameNode.object().get("entity_meshes"), ArrayGameNode.class);
        if (entityMeshesNode == null) {
            throw new IllegalArgumentException("Resources must include an \"entity_meshes\" attribute");
        }
        serverEntityMeshes = loadServerEntityMeshes(entityMeshesNode);
    }

    private Map<String, ServerEntityShape> loadServerEntityShapes(ArrayGameNode entityShapesNode) {
        Map<String, ServerEntityShape> serverEntityShapes = new HashMap<>();
        for (GameNode node : entityShapesNode.nodes()) {
            ObjectGameNode shapeNode = Game.checkGameNodeType(node, ObjectGameNode.class);
            StringGameNode idNode = Game.checkGameNodeType(shapeNode.object().get("id"), StringGameNode.class);
            if (idNode == null) {
                throw new IllegalArgumentException("Entity shapes must include an \"id\" attribute");
            }
            String id = idNode.value();
            ArrayGameNode polygonsNode = Game.checkGameNodeType(shapeNode.object().get("vertices"), ArrayGameNode.class);
            if (polygonsNode == null) {
                throw new IllegalArgumentException("Entity shapes must include an \"vertices\" attribute");
            }
            EntityVertex[][] entityPolygons = new EntityVertex[polygonsNode.nodes().length][];
            for (int k = 0, gameNodesLength = polygonsNode.nodes().length; k < gameNodesLength; k++) {
                GameNode polygonNode = polygonsNode.nodes()[k];
                ArrayGameNode verticesNode = Game.checkGameNodeType(polygonNode, ArrayGameNode.class);
                EntityVertex[] entityVertices = new EntityVertex[verticesNode.nodes().length];
                GameNode[] nodes = verticesNode.nodes();
                for (int j = 0, nodesLength = nodes.length; j < nodesLength; j++) {
                    GameNode vertexNode = nodes[j];
                    ArrayGameNode vertexDataNode = Game.checkGameNodeType(vertexNode, ArrayGameNode.class);
                    if (vertexDataNode.nodes().length != 2) {
                        throw new IllegalArgumentException("Entity shape vertices must contain exactly 2 items: position and uv coordinates");
                    }
                    ArrayGameNode vertexPositionNode = Game.checkGameNodeType(vertexDataNode.nodes()[0], ArrayGameNode.class);
                    if (vertexPositionNode.nodes().length != 3) {
                        throw new IllegalArgumentException("Entity shape vertex positions must contain exactly 3 items: XYZ");
                    }
                    double[] position = new double[3];
                    for (int i = 0; i < position.length; i++) {
                        position[i] = Game.checkGameNodeType(vertexPositionNode.nodes()[i], DoubleGameNode.class).value();
                    }
                    ArrayGameNode vertexTextureNode = Game.checkGameNodeType(vertexDataNode.nodes()[1], ArrayGameNode.class);
                    if (vertexTextureNode.nodes().length != 2) {
                        throw new IllegalArgumentException("Entity shape texture positions must contain exactly 2 items: UV");
                    }
                    double[] uv = new double[2];
                    for (int i = 0; i < uv.length; i++) {
                        uv[i] = Game.checkGameNodeType(vertexTextureNode.nodes()[i], DoubleGameNode.class).value();
                    }
                    entityVertices[j] = new EntityVertex((float) position[0], (float) position[1], (float) position[2], (float) uv[0], (float) uv[1]);
                }
                entityPolygons[k] = entityVertices;
            }

            ArrayGameNode indicesNode = Game.checkGameNodeType(shapeNode.object().get("indices"), ArrayGameNode.class);
            if (indicesNode == null) {
                throw new IllegalArgumentException("Entity shapes must include an \"indices\" attribute");
            }
            int[][] indices = new int[indicesNode.nodes().length][];
            for (int i = 0, indexNodesLength = indicesNode.nodes().length; i < indexNodesLength; i++) {
                ArrayGameNode polygonIndicesNode = Game.checkGameNodeType(indicesNode.nodes()[i], ArrayGameNode.class);
                int[] polygonIndices = new int[polygonIndicesNode.nodes().length];
                GameNode[] nodes = polygonIndicesNode.nodes();
                for (int j = 0, nodesLength = nodes.length; j < nodesLength; j++) {
                    polygonIndices[j] = (int) Game.checkGameNodeType(nodes[j], DoubleGameNode.class).value();
                }
                indices[i] = polygonIndices;
            }

            serverEntityShapes.put(id, new ServerEntityShape(id, entityPolygons, indices));
        }

        return serverEntityShapes;
    }

    private Map<String, ServerEntityTexture> loadServerEntityTextures(ArrayGameNode entityTexturesNode) {
        // TODO: Implement textures
        Map<String, ServerEntityTexture> serverEntityTextures = new HashMap<>();

        for (GameNode gameNode : entityTexturesNode.nodes()) {
            ObjectGameNode entityNode = Game.checkGameNodeType(gameNode, ObjectGameNode.class);
            StringGameNode idNode = Game.checkGameNodeType(entityNode.object().get("id"), StringGameNode.class);
            if (idNode == null) {
                throw new IllegalArgumentException("Entity textures must include an \"id\" attribute");
            }
            String id = idNode.value();
            serverEntityTextures.put(id, new ServerEntityTexture());
        }

        return serverEntityTextures;
    }

    private Map<String, ServerEntityMesh> loadServerEntityMeshes(ArrayGameNode entityMeshesNode) {
        Map<String, ServerEntityMesh> serverEntityMeshes = new HashMap<>();
        for (GameNode node : entityMeshesNode.nodes()) {
            ObjectGameNode meshNode = Game.checkGameNodeType(node, ObjectGameNode.class);
            StringGameNode meshIDNode = Game.checkGameNodeType(meshNode.object().get("id"), StringGameNode.class);
            if (meshIDNode == null) {
                throw new IllegalArgumentException("Entity meshes must contain an \"id\" attribute");
            }
            String meshID = meshIDNode.value();
            if (serverEntityMeshes.containsKey(meshID)) {
                throw new IllegalStateException("Already found an entity mesh with id \"" + meshID + "\"");
            }
            StringGameNode shapeIDNode = Game.checkGameNodeType(meshNode.object().get("shape_id"), StringGameNode.class);
            if (shapeIDNode == null) {
                throw new IllegalArgumentException("Entity meshes must contain an \"shape_id\" attribute");
            }
            String shapeID = shapeIDNode.value();
            ServerEntityShape serverEntityShape = serverEntityShapes.get(shapeID);
            if (serverEntityShape == null) {
                throw new IllegalArgumentException("Cannot find entity shape \"" + shapeID + "\"");
            }
            StringGameNode textureIDNode = Game.checkGameNodeType(meshNode.object().get("texture_id"), StringGameNode.class);
            if (textureIDNode == null) {
                throw new IllegalArgumentException("Entity meshes must contain an \"texture_id\" attribute");
            }
            String textureID = textureIDNode.value();
            ServerEntityTexture serverEntityTexture = serverEntityTextures.get(textureID);
            if (serverEntityTexture == null) {
                throw new IllegalArgumentException("Cannot find entity texture \"" + textureID + "\"");
            }
            ArrayGameNode childrenNode = Game.checkGameNodeType(meshNode.object().get("children"), ArrayGameNode.class);
            if (childrenNode == null) {
                throw new IllegalArgumentException("Entity meshes must contain an \"children\" array attribute");
            }
            serverEntityMeshes.put(meshID, new ServerEntityMesh(serverEntityShape, serverEntityTexture, loadServerEntityMeshes(childrenNode).values().toArray(new ServerEntityMesh[0])));
        }
        return serverEntityMeshes;
    }

    public ServerEntityMesh getEntityMesh(String meshID) {
        return serverEntityMeshes.get(meshID);
    }

    public Map<String, ServerEntityMesh> getAllServerEntityMeshes() {
        return serverEntityMeshes;
    }
}