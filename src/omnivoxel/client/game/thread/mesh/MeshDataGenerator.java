package omnivoxel.client.game.thread.mesh;

import omnivoxel.client.game.position.ChunkPosition;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

public final class MeshDataGenerator implements Runnable {
    private final Logger logger;
    private final BlockingQueue<MeshDataTask> meshDataTasks;
    private final BiConsumer<ChunkPosition, MeshData> loadChunk;

    public MeshDataGenerator(Logger logger, BiConsumer<ChunkPosition, MeshData> loadChunk) {
        this.logger = logger;
        this.meshDataTasks = new LinkedBlockingQueue<>();
        this.loadChunk = loadChunk;
    }

    public BlockingQueue<MeshDataTask> getMeshDataTasks() {
        return meshDataTasks;
    }

//    private void addPoint(
//            List<Integer> vertices,
//            List<Integer> indices,
//            Map<UniqueVertex, Integer> vertexIndexMap,
//            Vertex position,
//            int tx,
//            int ty,
//            BlockFace normal,
//            float r,
//            float g,
//            float b
//    ) {
//        UniqueVertex vertex = new UniqueVertex(
//                position,
//                new TextureVertex(tx, ty),
//                normal
//        );
//
//        if (!vertexIndexMap.containsKey(vertex)) {
//            int[] vertexData = ShapeHelper.packVertexData(position, ao, r, g, b, normal, tx, ty);
//            vertexIndexMap.put(vertex, vertices.size());
//            for (int data : vertexData) {
//                vertices.add(data);
//            }
//        }
//        indices.add(vertexIndexMap.get(vertex) / 2);
//    }

    private void addPoint(
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap,
            Vertex position,
            int tx,
            int ty,
            BlockFace normal,
            float r,
            float g,
            float b,
            Block[] blocks,
            int x, int y, int z
    ) {
        int ao = calculateAO(blocks, x, y, z, normal);

        UniqueVertex vertex = new UniqueVertex(
                position,
                new TextureVertex(tx, ty),
                normal
        );

        if (!vertexIndexMap.containsKey(vertex)) {
            int[] vertexData = ShapeHelper.packVertexData(position, ao, r, g, b, normal, tx, ty);
            vertexIndexMap.put(vertex, vertices.size());
            for (int data : vertexData) {
                vertices.add(data);
            }
        }
        indices.add(vertexIndexMap.get(vertex) / 2);
    }

    private int calculateAO(Block[] blocks, int x, int y, int z, BlockFace face) {
        int occlusion = switch (face) {
            case TOP -> isSolid(blocks, x, y + 1, z) ? 3 : 0;
            case BOTTOM -> isSolid(blocks, x, y - 1, z) ? 3 : 0;
            case NORTH -> isSolid(blocks, x, y, z - 1) ? 3 : 0;
            case SOUTH -> isSolid(blocks, x, y, z + 1) ? 3 : 0;
            case EAST -> isSolid(blocks, x + 1, y, z) ? 3 : 0;
            case WEST -> isSolid(blocks, x - 1, y, z) ? 3 : 0;
            default -> 0;
        };

        return occlusion;
    }

    private boolean isSolid(Block[] blocks, int x, int y, int z) {
        int index = calculateBlockIndex(x, y, z);
        if (index < 0 || index >= blocks.length) {
            return false;
        }
        return blocks[index] != null && !blocks[index].isTransparent();
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
//        addPoint(vertices, indices, vertexIndexMap, v1, 0, 0, normal, 0, 0, 0);
//        addPoint(vertices, indices, vertexIndexMap, v2, 0, 0, normal, 0, 0, 0);
//        addPoint(vertices, indices, vertexIndexMap, v3, 0, 0, normal, 0, 0, 0);
//
//        addPoint(vertices, indices, vertexIndexMap, v1, 0, 0, normal, 0, 0, 0);
//        addPoint(vertices, indices, vertexIndexMap, v3, 0, 0, normal, 0, 0, 0);
//        addPoint(vertices, indices, vertexIndexMap, v4, 0, 0, normal, 0, 0, 0);
    }

//    public MeshData generateEntityMeshData(Entity entity) {
//        List<Integer> vertices = new ArrayList<>();
//        List<Integer> indices = new ArrayList<>();
//        List<Integer> transparentVertices = new ArrayList<>();
//        List<Integer> transparentIndices = new ArrayList<>();
//        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();
//        Map<UniqueVertex, Integer> transparentVertexIndexMap = new HashMap<>();
//
//        int x = 16;
//        addCube(vertices, indices, vertexIndexMap, new Vertex(0, 0, 0), new Vertex(ShapeHelper.PIXEL * x, ShapeHelper.PIXEL * x, ShapeHelper.PIXEL * x));
//
//        ByteBuffer vertexBuffer = createBuffer(vertices);
//        ByteBuffer indexBuffer = createBuffer(indices);
//        ByteBuffer transparentVertexBuffer = createBuffer(transparentVertices);
//        ByteBuffer transparentIndexBuffer = createBuffer(transparentIndices);
//
//        return new GeneralMeshData(vertexBuffer, indexBuffer, transparentVertexBuffer, transparentIndexBuffer);
//    }

    public MeshData generateChunkMeshData(Block[] blocks) {
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
                        if (block.shouldRenderTransparentMesh()) {
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
                                    transparentVertexIndexMap, blocks
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
                                    vertexIndexMap, blocks
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
            Map<UniqueVertex, Integer> vertexIndexMap,
            Block[] blocks
    ) {
        if (shouldRenderFace(block, top, BlockFace.TOP, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.TOP, vertices, indices, vertexIndexMap, blocks);
        }
        if (shouldRenderFace(block, bottom, BlockFace.BOTTOM, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.BOTTOM, vertices, indices, vertexIndexMap, blocks);
        }
        if (shouldRenderFace(block, north, BlockFace.NORTH, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.NORTH, vertices, indices, vertexIndexMap, blocks);
        }
        if (shouldRenderFace(block, south, BlockFace.SOUTH, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.SOUTH, vertices, indices, vertexIndexMap, blocks);
        }
        if (shouldRenderFace(block, east, BlockFace.EAST, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.EAST, vertices, indices, vertexIndexMap, blocks);
        }
        if (shouldRenderFace(block, west, BlockFace.WEST, top, bottom, north, south, east, west)) {
            addFace(x, y, z, block, top, bottom, north, south, east, west, BlockFace.WEST, vertices, indices, vertexIndexMap, blocks);
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
            Map<UniqueVertex, Integer> vertexIndexMap,
            Block[] blocks
    ) {
        Shape shape = block.getShape(top, bottom, north, south, east, west);
        int[] uvCoordinates = block.getUVCoordinates(blockFace);
        Vertex[] faceVertices = shape.getVerticesOnFace(blockFace);
        int[] faceIndices = shape.getIndicesOnFace(blockFace);

        float temp_r, temp_g, temp_b;

        if (block.getState() == null) {
            temp_r = 0;
            temp_g = 0;
            temp_b = 0;
        } else {
            temp_r = block.getState()[0] / 32f + 0.5f;
            temp_g = block.getState()[1] / 32f + 0.5f;
            temp_b = block.getState()[2] / 32f + 0.5f;
        }

        for (int index : faceIndices) {
            Vertex pointPosition = faceVertices[index];
            Vertex blockInChunkPosition = pointPosition.add(x, y, z);
            addPoint(vertices,
                    indices,
                    vertexIndexMap,
                    blockInChunkPosition,
                    uvCoordinates[index * 2],
                    uvCoordinates[index * 2 + 1],
                    blockFace,
                    temp_r,
                    temp_g,
                    temp_b,
                    blocks,
                    x, y, z
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

    @Override
    public void run() {
        try {
            List<MeshDataTask> batch = new ArrayList<>();
            while (!Thread.currentThread().isInterrupted()) {
                if (meshDataTasks.drainTo(batch) == 0) {
                    Thread.sleep(1);
                    continue;
                }
                for (MeshDataTask meshDataTask : batch) {
                    MeshData meshData = generateChunkMeshData(meshDataTask.blocks());
                    loadChunk.accept(meshDataTask.chunkPosition(), meshData);
                }
                batch.clear();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("MeshDataGenerator interrupted", e);
        }
    }
}