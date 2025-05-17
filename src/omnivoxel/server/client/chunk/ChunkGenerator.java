package omnivoxel.server.client.chunk;

import core.biomes.*;
import core.structures.TestStructure;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.math.Position3D;
import omnivoxel.server.ServerWorld;
import omnivoxel.server.chunk.result.GeneratedChunk;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.structure.Structure;
import omnivoxel.server.client.structure.StructureBoundingBox;
import omnivoxel.server.client.structure.StructureSeed;
import omnivoxel.server.client.structure.StructureService;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkGenerator {
    private final ServerWorldDataService worldDataService;
    private final Set<Position3D> structureGeneratedChunks = ConcurrentHashMap.newKeySet();
    private final StructureService structureService;
    private final BiomeService biomeService;

    public ChunkGenerator(ServerWorldDataService worldDataService, BlockService blockService, BiomeService biomeService) {
        this.worldDataService = worldDataService;
        this.biomeService = biomeService;
        structureService = new StructureService();
        structureService.register(new TestStructure().initBlocks(blockService));
    }

    public static void main(String[] args) {
        BlockService blockService = new BlockService();
        BiomeService biomeService = new BiomeService(Map.of(new ClimateVector(0.0, 0.0, 0.7, 0.3, 0.0), new DesertBiome(blockService), new ClimateVector(0.0, 0.0, 0.7, 0.7, 0.0), new JungleBiome(blockService), new ClimateVector(0.0, 0.0, 0.3, 0.3, 0.0), new TundraBiome(blockService), new ClimateVector(0.0, 0.0, 0.3, 0.7, 0.0), new TaigaBiome(blockService), new ClimateVector(0.0, 0.0, 0.5, 0.5, 0.0), new PlainsBiome(blockService)));
        ChunkGenerator chunkGenerator = new ChunkGenerator(new BasicWorldDataService(new Random(0), new ServerWorld(), biomeService, blockService, new ConcurrentHashMap<>()), blockService, biomeService);

        int totalCount = 0;

        for (int i = 0; i < 100; i++) {
            long startTime = System.currentTimeMillis();
            int count = 0;
            while (System.currentTimeMillis() - startTime < 1000) {
                chunkGenerator.generateChunk(new Position3D(0, 0, 0));
                count++;
            }
            totalCount += count;

            System.out.printf("Chunk Generator Benchmark Results: {generated=" + count + ", average=" + totalCount / (i + 1) + "}\n");
        }
    }

    public GeneratedChunk generateChunk(Position3D position3D) {
        GeneratedChunk chunk = new EmptyGeneratedChunk();
        if (!worldDataService.shouldGenerateChunk(position3D)) {
            return chunk;
        }

        generateSurroundingChunks(position3D);

        for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
            int worldX = position3D.x() * ConstantGameSettings.CHUNK_WIDTH + x;
            for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
                int worldZ = position3D.z() * ConstantGameSettings.CHUNK_LENGTH + z;

                ClimateVector climateVector2D = worldDataService.getClimateVector2D(worldX, worldZ);
                for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {
                    int worldY = position3D.y() * ConstantGameSettings.CHUNK_HEIGHT + y;
                    chunk = chunk.setBlock(x, y, z, worldDataService.getBlockAt(position3D, worldX, worldY, worldZ, climateVector2D));
                }
            }
        }
        return chunk;
    }

    private void generateSurroundingChunks(Position3D position3D) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
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
                            Map<Position3D, ServerBlock> blocks = structure.getBlocks();
                            StructureBoundingBox boundingBox = structure.getBoundingBox();
                            Position3D origin = position3D.add(structure.getOrigin());
                            if (structureSeed.offset() != null) {
                                origin.add(structureSeed.offset());
                            }
                            int xl = boundingBox.getWidth();
                            int yl = boundingBox.getHeight();
                            int zl = boundingBox.getLength();
                            for (int X = 0; X < xl; X++) {
                                for (int Z = 0; Z < zl; Z++) {
                                    for (int Y = 0; Y < yl; Y++) {
                                        ServerBlock block = blocks.get(new Position3D(X, Y, Z));
                                        if (block != null) {
                                            worldDataService.queueBlock(origin.add(-worldX, -worldY, -worldZ), block);
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