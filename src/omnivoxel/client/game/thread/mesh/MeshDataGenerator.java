package omnivoxel.client.game.thread.mesh;

import omnivoxel.client.game.entity.Entity;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.meshData.GeneralMeshData;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.game.thread.mesh.shape.Shape;
import omnivoxel.client.game.thread.mesh.shape.util.ShapeHelper;
import omnivoxel.client.game.thread.mesh.vertex.TextureVertex;
import omnivoxel.client.game.thread.mesh.vertex.UniqueVertex;
import omnivoxel.client.game.thread.mesh.vertex.Vertex;
import omnivoxel.debug.Logger;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MeshDataGenerator {
    private final Logger logger;

    public MeshDataGenerator(Logger logger) {
        this.logger = logger;
    }

    private void addPoint(
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap,
            Vertex position,
            int tx,
            int ty,
            BlockFace normal
    ) {
        UniqueVertex vertex = new UniqueVertex(
                position,
                new TextureVertex(tx, ty),
                normal
        );

        if (!vertexIndexMap.containsKey(vertex)) {
            int[] vertexData = ShapeHelper.packVertexData(position, 0, 0, 0, normal, tx, ty);
            vertexIndexMap.put(vertex, vertices.size());
            for (int data : vertexData) {
                vertices.add(data);
            }
        }
        indices.add(vertexIndexMap.get(vertex) / 2);
    }

    private void addCube(
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap,
            Vertex position,
            Vertex size
    ) {
        Vertex tne = position.add(size.px(), size.py(), size.pz());
        Vertex tnw = position.add(0, size.py(), size.pz());

        Vertex tse = position.add(size.px(), size.py(), 0);
        Vertex tsw = position.add(0, size.py(), 0);

        Vertex bne = position.add(size.px(), 0, size.pz());
        Vertex bnw = position.add(0, 0, size.pz());

        Vertex bse = position.add(size.px(), 0, 0);
        Vertex bsw = position.add(0, 0, 0);

        addFace(vertices, indices, vertexIndexMap, tne, tnw, tsw, tse, BlockFace.TOP);
        addFace(vertices, indices, vertexIndexMap, bne, bnw, bsw, bse, BlockFace.BOTTOM);
        addFace(vertices, indices, vertexIndexMap, tne, tnw, bnw, bne, BlockFace.NORTH);
        addFace(vertices, indices, vertexIndexMap, tse, tsw, bsw, bse, BlockFace.SOUTH);
        addFace(vertices, indices, vertexIndexMap, tse, tne, bne, bse, BlockFace.EAST);
        addFace(vertices, indices, vertexIndexMap, tsw, tnw, bnw, bsw, BlockFace.WEST);
    }

    private void addFace(
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap,
            Vertex v1,
            Vertex v2,
            Vertex v3,
            Vertex v4,
            BlockFace normal
    ) {
        addPoint(vertices, indices, vertexIndexMap, v1, 0, 0, normal);
        addPoint(vertices, indices, vertexIndexMap, v2, 0, 0, normal);
        addPoint(vertices, indices, vertexIndexMap, v3, 0, 0, normal);

        addPoint(vertices, indices, vertexIndexMap, v1, 0, 0, normal);
        addPoint(vertices, indices, vertexIndexMap, v3, 0, 0, normal);
        addPoint(vertices, indices, vertexIndexMap, v4, 0, 0, normal);
    }

    public MeshData generateEntityMeshData(Entity entity) {
        List<Integer> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> transparentVertices = new ArrayList<>();
        List<Integer> transparentIndices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();
        Map<UniqueVertex, Integer> transparentVertexIndexMap = new HashMap<>();

        int x = 16;
        addCube(vertices, indices, vertexIndexMap, new Vertex(0, 0, 0), new Vertex(ShapeHelper.PIXEL * x, ShapeHelper.PIXEL * x, ShapeHelper.PIXEL * x));

        ByteBuffer vertexBuffer = createBuffer(vertices);
        ByteBuffer indexBuffer = createBuffer(indices);
        ByteBuffer transparentVertexBuffer = createBuffer(transparentVertices);
        ByteBuffer transparentIndexBuffer = createBuffer(transparentIndices);

        return new GeneralMeshData(vertexBuffer, indexBuffer, transparentVertexBuffer, transparentIndexBuffer);
    }

    public MeshData generateChunkMeshData(Block[] blocks, List<Block> palette) {
        List<Integer> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> transparentVertices = new ArrayList<>();
        List<Integer> transparentIndices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();
        Map<UniqueVertex, Integer> transparentVertexIndexMap = new HashMap<>();

        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    Block block = blocks[calculateBlockIndex(x, y, z)];
                    if (block != null) {
                        if (block.isTransparent()) {
                            generateBlockMeshData(
                                    x, y, z,
                                    block,
                                    blocks[calculateBlockIndex(x, y + 1, z)],
                                    blocks[calculateBlockIndex(x, y - 1, z)],
                                    blocks[calculateBlockIndex(x, y, z + 1)],
                                    blocks[calculateBlockIndex(x, y, z - 1)],
                                    blocks[calculateBlockIndex(x + 1, y, z)],
                                    blocks[calculateBlockIndex(x - 1, y, z)],
                                    transparentVertices,
                                    transparentIndices,
                                    transparentVertexIndexMap
                            );
                        } else {
                            generateBlockMeshData(
                                    x, y, z,
                                    block,
                                    blocks[calculateBlockIndex(x, y + 1, z)],
                                    blocks[calculateBlockIndex(x, y - 1, z)],
                                    blocks[calculateBlockIndex(x, y, z + 1)],
                                    blocks[calculateBlockIndex(x, y, z - 1)],
                                    blocks[calculateBlockIndex(x + 1, y, z)],
                                    blocks[calculateBlockIndex(x - 1, y, z)],
                                    vertices,
                                    indices,
                                    vertexIndexMap
                            );
                        }
                    }
                }
            }
        }

        ByteBuffer vertexBuffer = createBuffer(vertices);
        ByteBuffer indexBuffer = createBuffer(indices);
        ByteBuffer transparentVertexBuffer = createBuffer(transparentVertices);
        ByteBuffer transparentIndexBuffer = createBuffer(transparentIndices);

        return new GeneralMeshData(vertexBuffer, indexBuffer, transparentVertexBuffer, transparentIndexBuffer);
    }

    private int addVertex(List<Integer> vertices, Map<UniqueVertex, Integer> vertexMap, UniqueVertex vertex) {
        if (!vertexMap.containsKey(vertex)) {
            int index = vertices.size() / 3; // Assuming three coordinates per vertex.
            vertexMap.put(vertex, index);
            // Append the vertex data (here, simply the position; extend as needed).
            vertices.add((int) vertex.vertex().px());
            vertices.add((int) vertex.vertex().py());
            vertices.add((int) vertex.vertex().pz());
            return index;
        }
        return vertexMap.get(vertex);
    }

    private int getAdjacentBlock(int x, int y, int z, BlockFace blockFace) {
        return switch (blockFace) {
            case TOP -> calculateBlockIndex(x, y + 1, z);
            case BOTTOM -> calculateBlockIndex(x, y - 1, z);
            case NORTH -> calculateBlockIndex(x, y, z + 1);
            case SOUTH -> calculateBlockIndex(x, y, z - 1);
            case EAST -> calculateBlockIndex(x + 1, y, z);
            case WEST -> calculateBlockIndex(x - 1, y, z);
            default -> throw new IllegalStateException("Unexpected value: " + blockFace);
        };
    }

    private int calculateBlockIndex(int x, int y, int z) {
        return (x + 1) * (ConstantGameSettings.CHUNK_WIDTH + 2) * (ConstantGameSettings.CHUNK_LENGTH + 2) + (z + 1) * (ConstantGameSettings.CHUNK_LENGTH + 2) + (y + 1);
    }

    private void generateBlockMeshData(
            int x, int y, int z,
            Block block,
            Block top,
            Block bottom,
            Block north,
            Block south,
            Block east,
            Block west,
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap
    ) {
        if (shouldRenderFace(block, top, BlockFace.TOP, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.TOP, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, bottom, BlockFace.BOTTOM, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.BOTTOM, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, north, BlockFace.NORTH, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.NORTH, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, south, BlockFace.SOUTH, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.SOUTH, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, east, BlockFace.EAST, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.EAST, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, west, BlockFace.WEST, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.WEST, vertices, indices, vertexIndexMap);
        }
    }

    private boolean shouldRenderFace(
            Block originalBlock,
            Block adjacentBlock,
            BlockFace face,
            Block top,
            Block bottom,
            Block north,
            Block south,
            Block east,
            Block west
    ) {
        if (adjacentBlock == null) {
            return true;
        }
        return (!
                (
                        originalBlock.getShape(top, bottom, north, south, east, west).isFaceSolid(face) &&
                                adjacentBlock.getShape(top, bottom, north, south, east, west).isFaceSolid(face)
                )) || originalBlock.shouldRenderFace(face, adjacentBlock);
    }

    private void addFace(
            int x, int y, int z,
            Block block,
            Block top,
            Block bottom,
            Block north,
            Block south,
            Block east,
            Block west,
            BlockFace blockFace,
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap
    ) {
        Shape shape = block.getShape(top, bottom, north, south, east, west);
        int[] uvCoordinates = block.getUVCoordinates(blockFace);
        Vertex[] faceVertices = shape.getVerticesOnFace(blockFace);
        int[] faceIndices = shape.getIndicesOnFace(blockFace);

        for (int index : faceIndices) {
            Vertex pointPosition = faceVertices[index];
            Vertex blockInChunkPosition = pointPosition.add(x, y, z);
            addPoint(vertices,
                    indices,
                    vertexIndexMap,
                    blockInChunkPosition,
                    uvCoordinates[index * 2],
                    uvCoordinates[index * 2 + 1],
                    blockFace
            );
        }
    }

    private ByteBuffer createBuffer(List<Integer> data) {
        ByteBuffer buffer = MemoryUtil.memAlloc(data.size() * Integer.BYTES);
        try {
            for (int value : data) {
                buffer.putInt(value);
            }
            buffer.flip();
            return buffer;
        } catch (Exception e) {
            MemoryUtil.memFree(buffer);
            throw new RuntimeException("Error creating buffer", e);
        }
    }
}