package omnivoxel.server.client.chunk;

import core.structures.TreeStructure;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.math.Position3D;
import omnivoxel.server.PackageID;
import omnivoxel.server.ServerWorld;
import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.result.ChunkResult;
import omnivoxel.server.client.chunk.result.GeneratedChunk;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.structure.Structure;
import omnivoxel.server.client.structure.StructureBoundingBox;
import omnivoxel.server.client.structure.StructureSeed;
import omnivoxel.server.client.structure.StructureService;
import omnivoxel.world.chunk.Chunk;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkGenerator {
    private final ServerWorldDataService worldDataService;
    private final Set<Position3D> structureGeneratedChunks = ConcurrentHashMap.newKeySet();
    private final StructureService structureService;
    private final BiomeService biomeService;
    private final ServerWorld world;

    public ChunkGenerator(ServerWorldDataService worldDataService, BlockService blockService, BiomeService biomeService, ServerWorld world) {
        this.worldDataService = worldDataService;
        this.biomeService = biomeService;
        this.world = world;
        structureService = new StructureService();
        structureService.register(new TreeStructure().initBlocks(blockService));
    }

    private void sendChunkBytes(ChannelHandlerContext ctx, int x, int y, int z, byte[] chunk) {
        ByteBuf buffer = Unpooled.buffer();
        int length = 16 + chunk.length;
        buffer.writeInt(length);
        buffer.writeInt(PackageID.CHUNK.ordinal());
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBytes(chunk);
        ctx.channel().writeAndFlush(buffer);
    }

    public void generateChunk(ChunkTask task) {
        Position3D position3D = new Position3D(task.x(), task.y(), task.z());

        ChunkResult chunkResult;
        Chunk c = world.get(position3D);
        GeneratedChunk chunk = new EmptyGeneratedChunk();
        if (c != null) {
            for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
                for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
                    for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {

                        boolean border =
                                x == -1 || x == ConstantGameSettings.CHUNK_WIDTH ||
                                        y == -1 || y == ConstantGameSettings.CHUNK_HEIGHT ||
                                        z == -1 || z == ConstantGameSettings.CHUNK_LENGTH;

                        chunk = chunk.setBlock(x, y, z, border ? world.getBlock(position3D, x, y, z) : c.getBlock(x, y, z));
                    }
                }
            }

            chunkResult = GeneratedChunk.getResult(chunk);
        } else {
            if (worldDataService.shouldGenerateChunk(position3D)) {
                generateSurroundingChunks(position3D, 1);

                for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
                    int worldX = position3D.x() * ConstantGameSettings.CHUNK_WIDTH + x;
                    for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
                        int worldZ = position3D.z() * ConstantGameSettings.CHUNK_LENGTH + z;

                        ClimateVector climateVector2D = worldDataService.getClimateVector2D(worldX, worldZ);
                        for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {
                            int worldY = position3D.y() * ConstantGameSettings.CHUNK_HEIGHT + y;

                            boolean border =
                                    x == -1 || x == ConstantGameSettings.CHUNK_WIDTH ||
                                            y == -1 || y == ConstantGameSettings.CHUNK_HEIGHT ||
                                            z == -1 || z == ConstantGameSettings.CHUNK_LENGTH;

                            chunk = chunk.setBlock(x, y, z, worldDataService.getBlockAt(position3D, worldX, worldY, worldZ, border, climateVector2D));
                        }
                    }
                }
            }

            chunkResult = GeneratedChunk.getResult(chunk);
            world.add(position3D, chunkResult.chunk());
        }

        sendChunkBytes(task.ctx(), task.x(), task.y(), task.z(), chunkResult.bytes());
    }

    private void generateSurroundingChunks(Position3D position3D, int size) {
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                for (int z = -size; z <= size; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    generateChunkStructures(position3D.add(x, y, z));
                }
            }
        }
    }

    private void generateChunkStructures(Position3D position3D) {
        if (structureGeneratedChunks.add(position3D)) {
            for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
                int worldX = position3D.x() * ConstantGameSettings.CHUNK_WIDTH + x;
                for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
                    int worldZ = position3D.z() * ConstantGameSettings.CHUNK_LENGTH + z;

                    ClimateVector climateVector2D = worldDataService.getClimateVector2D(worldX, worldZ);
                    for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {
                        int worldY = position3D.y() * ConstantGameSettings.CHUNK_HEIGHT + y;
                        ClimateVector climateVector3D = worldDataService.getClimateVector3D(worldX, worldY, worldZ);

                        Biome biome = biomeService.generateBiome(climateVector2D);

                        StructureSeed structureSeed = structureService.getStructure(biome, worldX, worldY, worldZ, climateVector2D, climateVector3D);

                        if (structureSeed != null) {
                            Structure structure = structureSeed.structure();
                            Map<Position3D, PriorityServerBlock> blocks = structure.getBlocks();
                            StructureBoundingBox boundingBox = structure.getBoundingBox();
                            Position3D origin = structure.getOrigin();
                            if (structureSeed.offset() != null) {
                                origin.add(structureSeed.offset());
                            }
                            int xl = boundingBox.getWidth();
                            int yl = boundingBox.getHeight();
                            int zl = boundingBox.getLength();
                            for (int X = 0; X < xl; X++) {
                                for (int Z = 0; Z < zl; Z++) {
                                    for (int Y = 0; Y < yl; Y++) {
                                        PriorityServerBlock block = blocks.get(new Position3D(X, Y, Z));
                                        if (block != null) {
                                            Position3D pos = origin.add(X, Y, Z).add(worldX, worldY, worldZ);
                                            worldDataService.queueBlock(pos, block);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}