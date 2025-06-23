package omnivoxel.server.client.structure;

import core.biomes.ForestBiome;
import core.noise._2D.PerlinNoise;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

import java.util.ArrayList;
import java.util.List;

public class StructureService {
    private final List<Structure> structures;
    private final Noise2D treeNoise;

    public StructureService() {
        structures = new ArrayList<>();
        treeNoise = new PerlinNoise(0);
    }

    public void register(Structure structure) {
        structures.add(structure);
    }

    public StructureSeed getStructure(Biome biome, int x, int y, int z, ClimateVector climateVector2D, ClimateVector climateVector3D) {
        int yOffset = (int) (climateVector2D.get(0) - y);

        if (biome instanceof ForestBiome && yOffset == 0 && y > BasicWorldDataService.WATER_LEVEL) {
            double noiseValue = treeNoise.generate(x * 0.2, z * 0.2);
            if (noiseValue > 0.2) {
                return new StructureSeed(structures.getFirst(), null);
            }
        }
        return null;
    }
}