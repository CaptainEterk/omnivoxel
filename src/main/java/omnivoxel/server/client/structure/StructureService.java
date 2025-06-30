package omnivoxel.server.client.structure;

import core.biomes.ForestBiome;
import core.noise._2D.PerlinNoise;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.ChunkInfo;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

import java.util.ArrayList;
import java.util.List;

public class StructureService {
    private final List<Structure> structures;
    private final Noise2D treeNoise;
    private final BasicWorldDataService worldDataService;

    public StructureService(ServerWorldDataService worldDataService) {
        if (worldDataService instanceof BasicWorldDataService basicWorldDataService) {
            this.worldDataService = basicWorldDataService;
        } else {
            throw new IllegalArgumentException("Must be a BasicWorldDataService, not " + worldDataService.getClass());
        }
        structures = new ArrayList<>();
        treeNoise = new PerlinNoise(0);
    }

    public void register(Structure structure) {
        structures.add(structure);
    }

    public StructureSeed getStructure(int x, int y, int z, int worldX, int worldY, int worldZ, ChunkInfo chunkInfo) {
        int height = chunkInfo.get(0, int[][].class)[x + 1][z + 1];
        ClimateVector climateVector2D = chunkInfo.get(1, ClimateVector[][].class)[x + 1][z + 1];

        int yOffset = height - worldY;

        Biome biome = worldDataService.getBiome(worldX, worldY, worldZ, height, climateVector2D);

        if (biome instanceof ForestBiome && yOffset == 0 && y > BasicWorldDataService.WATER_LEVEL) {
            double noiseValue = treeNoise.generate(worldX * 0.2, worldZ * 0.2);
            if (noiseValue > 0.1) {
                return new StructureSeed(structures.getFirst(), null);
            }
        }
        return null;
    }
}