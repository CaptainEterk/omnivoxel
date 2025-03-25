package omnivoxel.server.client.chunk.biomeService;

import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;

import java.util.Map;

public class BiomeService {
    private final Map<ClimateVector, Biome> biomes;

    public BiomeService(Map<ClimateVector, Biome> biomes) {
        this.biomes = biomes;
    }

    public Biome generateBiome(ClimateVector climateVector) {
        final Biome[] closestBiome = new Biome[1];
        final double[] distance = {Double.MAX_VALUE};

        this.biomes.forEach(
                (vector, biome) -> {
                    double dis = vector.getDistance(climateVector);
                    if (dis < distance[0]) {
                        distance[0] = dis;
                        closestBiome[0] = biome;
                    }
                }
        );

        return closestBiome[0];
    }
}