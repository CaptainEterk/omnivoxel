package omnivoxel.client.game.thread.mesh;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.BlockStateWrapper;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.meshData.GeneralMeshData;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.game.thread.mesh.shape.Shape;
import omnivoxel.client.game.thread.mesh.shape.util.ShapeHelper;
import omnivoxel.client.game.thread.mesh.vertex.TextureVertex;
import omnivoxel.client.game.thread.mesh.vertex.UniqueVertex;
import omnivoxel.client.game.thread.mesh.vertex.Vertex;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.math.Position3D;
import omnivoxel.util.IndexCalculator;
import omnivoxel.util.log.Logger;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public final class MeshDataGenerator implements Runnable {
    private final Logger logger;
    private final BlockingQueue<MeshDataTask> meshDataTasks;
    private final BiConsumer<Position3D, MeshData> loadChunk;
    private final AtomicBoolean clientRunning;
    private final ClientWorldDataService worldDataService;

    public MeshDataGenerator(Logger logger, BiConsumer<Position3D, MeshData> loadChunk, AtomicBoolean clientRunning, ClientWorldDataService worldDataService) {
        this.logger = logger;
        this.clientRunning = clientRunning;
        this.worldDataService = worldDataService;
        this.meshDataTasks = new LinkedBlockingQueue<>();
        this.loadChunk = loadChunk;
    }

    public BlockingQueue<MeshDataTask> getMeshDataTasks() {
        return meshDataTasks;
    }

    private void addPoint(List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap, Vertex position, int tx, int ty, BlockFace normal, float r, float g, float b) {
        UniqueVertex vertex = new UniqueVertex(position, new TextureVertex(tx, ty), normal);

        if (!vertexIndexMap.containsKey(vertex)) {
            int[] vertexData = ShapeHelper.packVertexData(position, 0, r, g, b, normal, tx, ty);
            vertexIndexMap.put(vertex, vertices.size());
            for (int data : vertexData) {
                vertices.add(data);
            }
        }
        indices.add(vertexIndexMap.get(vertex) / 2);
    }

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
                    Block block = blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z)];
                    if (block != null) {
                        if (block.shouldRenderTransparentMesh()) {
                            generateBlockMeshData(x, y, z, block, blocks[IndexCalculator.calculateBlockIndexPadded(x, y + 1, z)], blocks[IndexCalculator.calculateBlockIndexPadded(x, y - 1, z)], blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z + 1)], blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z - 1)], blocks[IndexCalculator.calculateBlockIndexPadded(x + 1, y, z)], blocks[IndexCalculator.calculateBlockIndexPadded(x - 1, y, z)], transparentVertices, transparentIndices, transparentVertexIndexMap);
                        } else {
                            generateBlockMeshData(x, y, z, block, blocks[IndexCalculator.calculateBlockIndexPadded(x, y + 1, z)], blocks[IndexCalculator.calculateBlockIndexPadded(x, y - 1, z)], blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z + 1)], blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z - 1)], blocks[IndexCalculator.calculateBlockIndexPadded(x + 1, y, z)], blocks[IndexCalculator.calculateBlockIndexPadded(x - 1, y, z)], vertices, indices, vertexIndexMap);
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

    private void generateBlockMeshData(int x, int y, int z, Block block, Block top, Block bottom, Block north, Block south, Block east, Block west, List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap) {
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

    private boolean shouldRenderFace(Block originalBlock, Block adjacentBlock, BlockFace face, Block top, Block bottom, Block north, Block south, Block east, Block west) {
        if (adjacentBlock == null) {
            return true;
        }
        return (!(originalBlock.getShape(top, bottom, north, south, east, west).isFaceSolid(face) && adjacentBlock.getShape(top, bottom, north, south, east, west).isFaceSolid(face))) || originalBlock.shouldRenderFace(face, adjacentBlock);
    }

    private void addFace(int x, int y, int z, Block block, Block top, Block bottom, Block north, Block south, Block east, Block west, BlockFace blockFace, List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap) {
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
            temp_r = block.getState()[0] / 32f;
            temp_g = block.getState()[1] / 32f;
            temp_b = block.getState()[2] / 32f;
        }

        for (int index : faceIndices) {
            Vertex pointPosition = faceVertices[index];
            Vertex position = pointPosition.add(x, y, z);
            addPoint(vertices, indices, vertexIndexMap, position, uvCoordinates[index * 2], uvCoordinates[index * 2 + 1], blockFace, temp_r, temp_g, temp_b);
        }
    }

    private ByteBuffer createBuffer(List<Integer> data) {
        if (data.isEmpty()) {
            return null;
        }
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
            while (!Thread.currentThread().isInterrupted() && clientRunning.get()) {
                if (meshDataTasks.drainTo(batch) == 0) {
                    Thread.sleep(1);
                    continue;
                }
                while (!batch.isEmpty()) {
                    MeshDataTask meshDataTask = batch.removeFirst();
                    MeshData meshData = generateChunkMeshData(unpackChunk(meshDataTask.blocks()));
//                    logger.info(meshData.toString());
                    loadChunk.accept(meshDataTask.position3D(), meshData);
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("MeshDataGenerator interrupted", e);
        } finally {
            System.out.println("MeshDataGenerator worker thread stopped");
        }
    }

    private Block[] unpackChunk(ByteBuf byteBuf) {
        omnivoxel.world.block.Block[] palette = new omnivoxel.world.block.Block[byteBuf.getShort(20)];
        int index = 22;
        for (int i = 0; i < palette.length; i++) {
            StringBuilder blockID = new StringBuilder();
            short paletteLength = byteBuf.getShort(index);
            int j;
            for (j = 2; j < paletteLength + 2; j++) {
                byte b = byteBuf.getByte(index + j);
                blockID.append((char) b);
            }
            short blockStateCount = byteBuf.getShort(index + j);
            j += 2;
            int[] blockState = new int[blockStateCount];
            for (int k = 0; k < blockStateCount; k++) {
                blockState[k] = byteBuf.getInt(index + j);
                j += 4;
            }
            palette[i] = new omnivoxel.world.block.Block(blockID.toString(), blockState.length == 0 ? null : blockState);
            index += j;
        }

        Block[] blocks = new Block[ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED];
        for (int i = 0; i < ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED && index < byteBuf.readableBytes(); ) {
            int blockID = byteBuf.getInt(index);
            int blockCount = byteBuf.getInt(index + 4);
            int oi = i;
            for (; i < blockCount + oi; i++) {
                blocks[i] = worldDataService.getBlock(palette[blockID].id(), palette[blockID].blockState());
                if (palette[blockID].blockState() != null && !(blocks[i] instanceof BlockStateWrapper)) {
                    blocks[i] = new BlockStateWrapper(blocks[i], palette[blockID].blockState());
                    worldDataService.addBlock(blocks[i]);
                }
            }
            index += 8;
        }

        byteBuf.release();

        return blocks;
    }
}