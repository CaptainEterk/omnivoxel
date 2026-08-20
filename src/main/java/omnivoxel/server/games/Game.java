package omnivoxel.server.games;

import omnivoxel.common.block.hitbox.BlockHitbox;
import omnivoxel.common.block.shape.BlockShape;
import omnivoxel.common.block.shape.BlockVertex;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3DProvider;
import omnivoxel.util.game.nodes.*;
import omnivoxel.util.log.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class Game {
    @SuppressWarnings("unchecked")
    public static <T extends GameNode> T checkGameNodeType(GameNode gameNode, Class<T> clazz) {
        if (gameNode == null) {
            return null;
        }
        if (clazz.isInstance(gameNode)) {
            return (T) gameNode;
        }
        if (gameNode instanceof StringGameNode stringGameNode) {
            throw new IllegalArgumentException("Expected " + clazz.getSimpleName() + " but got \"" + stringGameNode.value() + "\"");
        }
        throw new IllegalArgumentException("Expected " + clazz.getSimpleName() + " but got " + gameNode.getClass().getSimpleName());
    }

    public static void loadNoises(ArrayGameNode noises, long seed) {
        Random random = new Random(seed + 1);
        for (GameNode node : noises.nodes()) {
            loadNoise(checkGameNodeType(node, ObjectGameNode.class), random.nextLong());
        }
    }

    private static void loadNoise(ObjectGameNode noise, long seed) {
        String id = checkGameNodeType(noise.object().get("id"), StringGameNode.class).value();
        double[] octaves = Arrays.stream(checkGameNodeType(noise.object().get("octaves"), ArrayGameNode.class).nodes())
                .mapToDouble(gameNode -> checkGameNodeType(gameNode, DoubleGameNode.class).value())
                .toArray();
        double firstOctave = checkGameNodeType(noise.object().get("first_octave"), DoubleGameNode.class).value();
        Logger.debug("Registered noise: " + id);
        Noise3DProvider.registerNoise(id, new Noise3D(octaves, (int) firstOctave, seed));
    }

    public static void loadBlocks(ObjectGameNode worldGeneratorNode, ServerBlockService blockService) {
        ArrayGameNode blocks = Game.checkGameNodeType(worldGeneratorNode.object().get("blocks"), ArrayGameNode.class);
        for (GameNode node : blocks.nodes()) {
            ObjectGameNode blockObjectGameNode = Game.checkGameNodeType(node, ObjectGameNode.class);
            String id = Game.checkGameNodeType(blockObjectGameNode.object().get("id"), StringGameNode.class).value();
            ArrayGameNode blockStates = Game.checkGameNodeType(blockObjectGameNode.object().get("block_states"), ArrayGameNode.class);
            for (GameNode stateNode : blockStates.nodes()) {
                ObjectGameNode objectStateNode = Game.checkGameNodeType(stateNode, ObjectGameNode.class);
                String blockState = Game.checkGameNodeType(objectStateNode.object().get("id"), StringGameNode.class).value();
                String blockShape = Game.checkGameNodeType(objectStateNode.object().get("block_shape"), StringGameNode.class).value();
                String blockHitbox = Game.checkGameNodeType(objectStateNode.object().get("block_hitbox"), StringGameNode.class).value();
                boolean transparentMesh = Game.checkGameNodeType(objectStateNode.object().get("transparent_mesh"), BooleanGameNode.class).value();
                boolean decorationMesh = Game.checkGameNodeType(objectStateNode.object().get("decoration_mesh"), BooleanGameNode.class).value();
                boolean isSelfOccluded = Game.checkGameNodeType(objectStateNode.object().get("self_occluded"), BooleanGameNode.class).value();
                BooleanGameNode rotatableNode = Game.checkGameNodeType(objectStateNode.object().get("rotatable"), BooleanGameNode.class);
                boolean rotatable = rotatableNode != null && rotatableNode.value();
                if (rotatableNode == null) {
                    Logger.warn("Block \"" + id + "\" should have a \"rotatable\" attribute (default = false)");
                }
                BooleanGameNode canPlaceOnNode = Game.checkGameNodeType(objectStateNode.object().get("can_place_on"), BooleanGameNode.class);
                boolean canPlaceOn = canPlaceOnNode == null || canPlaceOnNode.value();
                if (canPlaceOnNode == null) {
                    Logger.warn("Block \"" + id + "\" should have a \"can_place_on\" attribute (default = true)");
                }
                BooleanGameNode partOfGroundNode = Game.checkGameNodeType(objectStateNode.object().get("part_of_ground"), BooleanGameNode.class);
                boolean partOfGround = partOfGroundNode == null || partOfGroundNode.value();
                if (partOfGroundNode == null) {
                    Logger.warn("Block \"" + id + "\" should have a \"part_of_ground\" attribute (default = true)");
                }
                ObjectGameNode texture = Game.checkGameNodeType(objectStateNode.object().get("texture"), ObjectGameNode.class);
                ArrayGameNode lightEmittingNode = Game.checkGameNodeType(objectStateNode.object().get("light_emitting"), ArrayGameNode.class);
                byte[][] lightEmitting = new byte[6][3];
                if (lightEmittingNode != null) {
                    if (lightEmittingNode.nodes().length == 6) {
                        for (int i = 0; i < 6; i++) {
                            ArrayGameNode faceLightEmittingNode = Game.checkGameNodeType(lightEmittingNode.nodes()[i], ArrayGameNode.class);
                            if (faceLightEmittingNode.nodes().length != 3) {
                                throw new IllegalArgumentException("light_emitting attribute must have four values for rgb and skylight");
                            }
                            for (int j = 0; j < 3; j++) {
                                lightEmitting[i][j] = (byte) Math.clamp(Game.checkGameNodeType(faceLightEmittingNode.nodes()[j], DoubleGameNode.class).value(), 0, 15);
                            }
                        }
                    } else if (lightEmittingNode.nodes().length == 3) {
                        for (int i = 0; i < 6; i++) {
                            for (int j = 0; j < 3; j++) {
                                lightEmitting[i][j] = (byte) Math.clamp(Game.checkGameNodeType(lightEmittingNode.nodes()[j], DoubleGameNode.class).value(), 0, 15);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("\"light_diffusing\" attribute must be either an array of 6 arrays (for RGB and skylight diffusing per face), or an array of 4 values (for RGB and skylight diffusing for every face)");
                    }
                }
                ArrayGameNode lightDiffusingNode = Game.checkGameNodeType(objectStateNode.object().get("light_diffusing"), ArrayGameNode.class);
                byte[][] lightDefusing = new byte[6][4];
                if (lightDiffusingNode != null) {
                    if (lightDiffusingNode.nodes().length == 6) {
                        for (int i = 0; i < 6; i++) {
                            ArrayGameNode faceLightDefusingNode = Game.checkGameNodeType(lightDiffusingNode.nodes()[i], ArrayGameNode.class);
                            if (faceLightDefusingNode.nodes().length != 4) {
                                throw new IllegalArgumentException("\"light_diffusing\" attribute must have four values for rgb and skylight");
                            }
                            for (int j = 0; j < 4; j++) {
                                lightDefusing[i][j] = (byte) Math.clamp(Game.checkGameNodeType(faceLightDefusingNode.nodes()[j], DoubleGameNode.class).value(), 1, 15);
                            }
                        }
                    } else if (lightDiffusingNode.nodes().length == 4) {
                        for (int i = 0; i < 6; i++) {
                            for (int j = 0; j < 4; j++) {
                                lightDefusing[i][j] = (byte) Math.clamp(Game.checkGameNodeType(lightDiffusingNode.nodes()[j], DoubleGameNode.class).value(), 1, 15);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("\"light_diffusing\" attribute must be either an array of 6 arrays (for RGB and skylight diffusing per face), or an array of 4 values (for RGB and skylight diffusing for every face)");
                    }
                } else {
                    throw new IllegalArgumentException("Blocks must have a \"light_diffusing\" attribute");
                }
                String uvMapping = Game.checkGameNodeType(texture.object().get("uv_mapping"), StringGameNode.class).value();
                double[][] uvCoords = new double[6][];
                if (Objects.equals(uvMapping, "face")) {
                    double[] coords = Arrays.stream(Game.checkGameNodeType(texture.object().get("uv_coords"), ArrayGameNode.class).nodes()).mapToDouble((gn) -> Game.checkGameNodeType(gn, DoubleGameNode.class).value()).toArray();
                    for (int i = 0; i < 6; i++) {
                        uvCoords[i] = coords;
                    }
                } else if (Objects.equals(uvMapping, "cube")) {
                    ArrayGameNode faceNodes = Game.checkGameNodeType(texture.object().get("uv_coords"), ArrayGameNode.class);
                    if (faceNodes.nodes().length != 6) {
                        throw new IllegalArgumentException("Cube mapping must provide 6 UV arrays, one per face");
                    }

                    for (int i = 0; i < 6; i++) {
                        ArrayGameNode faceArray = Game.checkGameNodeType(faceNodes.nodes()[i], ArrayGameNode.class);
                        double[] faceCoords = Arrays.stream(faceArray.nodes())
                                .mapToDouble(gn -> Game.checkGameNodeType(gn, DoubleGameNode.class).value())
                                .toArray();
                        uvCoords[i] = faceCoords;
                    }
                } else {
                    throw new IllegalArgumentException("\"" + uvMapping + "\" is not a valid uv_mapping");
                }

                blockService.registerServerBlock(new ServerBlock(ServerBlock.createID(id, blockState), blockShape, uvCoords, transparentMesh, decorationMesh, isSelfOccluded, rotatable, canPlaceOn, partOfGround, lightEmitting, lightDefusing, blockHitbox));
            }
        }

        blockService.registerServerBlock(ServerBlock.AIR);
    }

    public static void loadBlockShapes(String gameID, ObjectGameNode worldGeneratorNode, Map<String, BlockShape> blockShapeCache) {
        ArrayGameNode blocksShapes = Game.checkGameNodeType(worldGeneratorNode.object().get("block_shapes"), ArrayGameNode.class);
        for (GameNode node : blocksShapes.nodes()) {
            ObjectGameNode blockShapeObjectGameNode = Game.checkGameNodeType(node, ObjectGameNode.class);
            String id = Game.checkGameNodeType(blockShapeObjectGameNode.object().get("id"), StringGameNode.class).value();
            ArrayGameNode verticesNode = Game.checkGameNodeType(blockShapeObjectGameNode.object().get("vertices"), ArrayGameNode.class);
            BlockVertex[][] vertices = new BlockVertex[6][];
            for (int i = 0; i < 6; i++) {
                BlockVertex[] vs = Arrays.stream(Game.checkGameNodeType(verticesNode.nodes()[i], ArrayGameNode.class).nodes()).map((gn) -> {
                    double[] vertexArray = Arrays.stream(Game.checkGameNodeType(gn, ArrayGameNode.class).nodes()).mapToDouble((vgn) -> Game.checkGameNodeType(vgn, DoubleGameNode.class).value()).toArray();
                    return new BlockVertex((float) vertexArray[0], (float) vertexArray[1], (float) vertexArray[2]);
                }).toArray(BlockVertex[]::new);
                vertices[i] = vs;
            }

            ArrayGameNode indicesNode = Game.checkGameNodeType(blockShapeObjectGameNode.object().get("indices"), ArrayGameNode.class);
            int[][] indices = new int[6][];
            for (int i = 0; i < 6; i++) {
                int[] vs = Arrays.stream(Game.checkGameNodeType(indicesNode.nodes()[i], ArrayGameNode.class).nodes()).mapToInt((gn) -> (int) Game.checkGameNodeType(gn, DoubleGameNode.class).value()).toArray();
                indices[i] = vs;
            }

            boolean[] solid;
            ArrayGameNode solidNode = Game.checkGameNodeType(blockShapeObjectGameNode.object().get("solid"), ArrayGameNode.class);
            if (solidNode != null && solidNode.nodes().length == 6) {
                GameNode[] solidNodes = solidNode.nodes();
                solid = new boolean[solidNodes.length];
                for (int i = 0; i < solidNodes.length; i++) {
                    solid[i] = Game.checkGameNodeType(solidNodes[i], BooleanGameNode.class).value();
                }
            } else {
                Logger.warn("attribute \"solid\" for block shape \"" + id + "\" must have a length of 6 (one for each face)");
                solid = new boolean[6];
            }

            boolean[] coverable;
            ArrayGameNode coverableNode = Game.checkGameNodeType(blockShapeObjectGameNode.object().get("coverable"), ArrayGameNode.class);
            if (coverableNode != null && coverableNode.nodes().length == 6) {
                GameNode[] coverableNodes = coverableNode.nodes();
                coverable = new boolean[coverableNodes.length];
                for (int i = 0; i < coverableNodes.length; i++) {
                    coverable[i] = Game.checkGameNodeType(coverableNodes[i], BooleanGameNode.class).value();
                }
            } else {
                Logger.warn("attribute \"coverable\" for block shape \"" + id + "\" must have a length of 6 (one for each face)");
                coverable = new boolean[6];
            }

            boolean[] coversOppositeSelfFace;
            ArrayGameNode coversOppositeSelfFaceNode = Game.checkGameNodeType(blockShapeObjectGameNode.object().get("covers_opposite_self_face"), ArrayGameNode.class);
            if (coversOppositeSelfFaceNode != null && coversOppositeSelfFaceNode.nodes().length == 6) {
                GameNode[] coversOppositeSelfFaceNodes = coversOppositeSelfFaceNode.nodes();
                coversOppositeSelfFace = new boolean[coversOppositeSelfFaceNodes.length];
                for (int i = 0; i < coversOppositeSelfFaceNodes.length; i++) {
                    coversOppositeSelfFace[i] = Game.checkGameNodeType(coversOppositeSelfFaceNodes[i], BooleanGameNode.class).value();
                }
            } else {
                Logger.warn("attribute \"covers_opposite_self_face\" for block shape \"" + id + "\" must have a length of 6 (one for each face)");
                coversOppositeSelfFace = new boolean[6];
            }

            blockShapeCache.put(gameID + ":" + id, new BlockShape(gameID + ":" + id, vertices, indices, solid, coverable, coversOppositeSelfFace));
        }

        blockShapeCache.put(BlockShape.EMPTY_BLOCK_SHAPE_STRING, BlockShape.EMPTY_BLOCK_SHAPE);
    }

    public static void loadBlockHitboxes(String gameID, ObjectGameNode worldGeneratorNode, Map<String, BlockHitbox[]> blockHitboxCache) {
        ArrayGameNode blockHitboxes = Game.checkGameNodeType(worldGeneratorNode.object().get("block_hitboxes"), ArrayGameNode.class);
        for (GameNode node : blockHitboxes.nodes()) {
            ObjectGameNode hitboxNode = Game.checkGameNodeType(node, ObjectGameNode.class);
            String id = Game.checkGameNodeType(hitboxNode.object().get("id"), StringGameNode.class).value();

            ArrayGameNode hitboxesNode = Game.checkGameNodeType(hitboxNode.object().get("hitboxes"), ArrayGameNode.class);

            BlockHitbox[] hitboxes = new BlockHitbox[hitboxesNode.nodes().length];
            GameNode[] nodes = hitboxesNode.nodes();
            for (int i = 0; i < nodes.length; i++) {
                ObjectGameNode objectGameNode = Game.checkGameNodeType(nodes[i], ObjectGameNode.class);
                ArrayGameNode minHitbox = Game.checkGameNodeType(objectGameNode.object().get("min"), ArrayGameNode.class);
                ArrayGameNode maxHitbox = Game.checkGameNodeType(objectGameNode.object().get("max"), ArrayGameNode.class);

                BooleanGameNode isVolumeNode = Game.checkGameNodeType(objectGameNode.object().get("volume"), BooleanGameNode.class);
                boolean isVolume = isVolumeNode != null && isVolumeNode.value();
                float speed;
                if (isVolume) {
                    speed = (float) Game.checkGameNodeType(objectGameNode.object().get("speed"), DoubleGameNode.class).value();
                } else {
                    speed = 0;
                }

                BooleanGameNode isGroundNode = Game.checkGameNodeType(objectGameNode.object().get("ground"), BooleanGameNode.class);
                boolean isGround = isGroundNode != null && isGroundNode.value();

                double minX = Game.checkGameNodeType(minHitbox.nodes()[0], DoubleGameNode.class).value();
                double minY = Game.checkGameNodeType(minHitbox.nodes()[1], DoubleGameNode.class).value();
                double minZ = Game.checkGameNodeType(minHitbox.nodes()[2], DoubleGameNode.class).value();

                double maxX = Game.checkGameNodeType(maxHitbox.nodes()[0], DoubleGameNode.class).value();
                double maxY = Game.checkGameNodeType(maxHitbox.nodes()[1], DoubleGameNode.class).value();
                double maxZ = Game.checkGameNodeType(maxHitbox.nodes()[2], DoubleGameNode.class).value();
                hitboxes[i] = new BlockHitbox(
                        (float) minX, (float) minY, (float) minZ,
                        (float) maxX, (float) maxY, (float) maxZ,
                        new BlockHitbox.BlockHitboxVolumeProperties(isVolume, speed, isGround)
                );
            }

            blockHitboxCache.put(gameID + ":" + id, hitboxes);
        }

        blockHitboxCache.put(BlockHitbox.EMPTY_BLOCK_HITBOX_STRING, BlockHitbox.EMPTY_BLOCK_HITBOX);
    }
}
