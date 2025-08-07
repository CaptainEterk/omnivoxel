package omnivoxel.client.game.graphics.opengl.mesh.generators;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.BlockStateWrapper;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.ChunkMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.TextureVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.UniqueVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.client.game.graphics.opengl.shape.util.ShapeHelper;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.util.IndexCalculator;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.block.BlockService;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk.SingleBlockChunk;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;

public class ChunkMeshDataGenerator {
    private static final omnivoxel.world.block.Block air = new omnivoxel.world.block.Block("omnivoxel:air");
    private final ClientWorldDataService worldDataService;
    private final BlockService blockService;

    public ChunkMeshDataGenerator(ClientWorldDataService worldDataService) {
        this.worldDataService = worldDataService;
        blockService = new BlockService();
    }

    private void addPoint(List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap, Vertex position, int tx, int ty, BlockFace normal, float r, float g, float b, int type) {
        UniqueVertex vertex = new UniqueVertex(position, new TextureVertex(tx, ty), normal);

        if (!vertexIndexMap.containsKey(vertex)) {
            int[] vertexData = ShapeHelper.packVertexData(position, 0, r, g, b, normal, tx, ty, type);
            vertexIndexMap.put(vertex, vertices.size());
            for (int data : vertexData) {
                vertices.add(data);
            }
        }
        indices.add(vertexIndexMap.get(vertex) / 3);
    }

    private MeshData generateChunkMeshData(Block[] blocks, Position3D position3D) {
        List<Integer> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> transparentVertices = new ArrayList<>();
        List<Integer> transparentIndices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();
        Map<UniqueVertex, Integer> transparentVertexIndexMap = new HashMap<>();

        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    int index = IndexCalculator.calculateBlockIndexPadded(x, y, z);
                    Block block = blocks[index];
                    if (block != null) {
                        if (block.shouldRenderTransparentMesh()) {
                            generateBlockMeshData(
                                    x,
                                    y,
                                    z,
                                    block,
                                    blocks[index + BlockFace.TOP.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.BOTTOM.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.NORTH.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.SOUTH.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.EAST.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.WEST.getPaddedNeighborOffset()],
                                    transparentVertices,
                                    transparentIndices,
                                    transparentVertexIndexMap
                            );
                        } else {
                            generateBlockMeshData(
                                    x,
                                    y,
                                    z,
                                    block,
                                    blocks[index + BlockFace.TOP.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.BOTTOM.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.NORTH.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.SOUTH.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.EAST.getPaddedNeighborOffset()],
                                    blocks[index + BlockFace.WEST.getPaddedNeighborOffset()],
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

        return new ChunkMeshData(vertexBuffer, indexBuffer, transparentVertexBuffer, transparentIndexBuffer, position3D);
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
        if (Objects.equals(adjacentBlock.getModID(), "omnivoxel:air") && !Objects.equals(originalBlock.getModID(), "omnivoxel:air")) {
            return true;
        }
        return (
                !(
                        originalBlock.getShape(top, bottom, north, south, east, west).isFaceSolid(face) &&
                                adjacentBlock.getShape(top, bottom, north, south, east, west).isFaceSolid(face)
                )
        ) && originalBlock.shouldRenderFace(face, adjacentBlock);
    }

    private void addFace(int x, int y, int z, Block block, Block top, Block bottom, Block north, Block south, Block east, Block west, BlockFace blockFace, List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap) {
        Shape shape = block.getShape(top, bottom, north, south, east, west);
        int[] uvCoordinates = block.getUVCoordinates(blockFace);
        Vertex[] faceVertices = shape.getVerticesOnFace(blockFace);
        int[] faceIndices = shape.getIndicesOnFace(blockFace);

        // TODO: Add lighting
        float temp_r, temp_g, temp_b;

        if (block.getState() == null) {
            temp_r = 0;
            temp_g = 0;
            temp_b = 0;
        } else if (block.getState().length == 1) {
            temp_r = block.getState()[0] / 32f;
            temp_g = block.getState()[0] / 32f;
            temp_b = block.getState()[0] / 32f;
        } else if (block.getState().length == 3) {
            temp_r = block.getState()[0] / 32f;
            temp_g = block.getState()[1] / 32f;
            temp_b = block.getState()[2] / 32f;
        } else {
            temp_r = 0;
            temp_g = 0;
            temp_b = 0;
        }


        for (int index : faceIndices) {
            Vertex pointPosition = faceVertices[index];
            Vertex position = pointPosition.add(x, y, z);
            // TODO: Replace with game dependent point type
            addPoint(vertices, indices, vertexIndexMap, position, uvCoordinates[index * 2], uvCoordinates[index * 2 + 1], blockFace, temp_r, temp_g, temp_b, Objects.equals(block.getModID(), "core:water_source_block") ? 1 : 0);
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

    private ChunkBlockData unpackChunk(ByteBuf byteBuf) {
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

        Chunk<omnivoxel.world.block.Block> chunk = new SingleBlockChunk<>(air);
        Block[] blocks = new Block[ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED];
        int x = 0, y = 0, z = 0;

        for (int i = 0; i < ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED; ) {
            int blockID = byteBuf.getInt(index);
            int blockCount = byteBuf.getInt(index + 4);
            index += 8;

            for (int j = 0; j < blockCount && i + j < ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED; j++) {
                int blockIndex = i + j;

                blocks[blockIndex] = worldDataService.getBlock(palette[blockID].id(), palette[blockID].blockState());
                if (palette[blockID].blockState() != null && !(blocks[blockIndex] instanceof BlockStateWrapper)) {
                    blocks[blockIndex] = new BlockStateWrapper(blocks[blockIndex], palette[blockID].blockState());
                    worldDataService.addBlock(blocks[blockIndex]);
                }

                if (x < ConstantGameSettings.CHUNK_WIDTH &&
                        y < ConstantGameSettings.CHUNK_HEIGHT &&
                        z < ConstantGameSettings.CHUNK_LENGTH) {
                    chunk = chunk.setBlock(x, y, z, blockService.getBlock(palette[blockID].id(), palette[blockID].blockState()));
                }

                y++;
                if (y >= ConstantGameSettings.PADDED_HEIGHT) {
                    y = 0;
                    z++;
                    if (z >= ConstantGameSettings.PADDED_LENGTH) {
                        z = 0;
                        x++;
                    }
                }
            }

            i += blockCount;
        }


        byteBuf.release();

        return new ChunkBlockData(chunk, blocks);
    }

    public MeshData generateMeshData(ByteBuf blocks, Position3D position3D, ClientWorld world) {
        ChunkBlockData chunk = unpackChunk(blocks);
        world.addChunkData(position3D, chunk.chunk());
        return generateChunkMeshData(chunk.blocks(), position3D);
    }
}