package omnivoxel.server.client.chunk.worldDataService;

import core.biomes.*;
import core.noise.FractionalBrownianNoise;
import core.noise.PerlinNoise;
import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.block.StructureSeed;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;
import omnivoxel.server.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

// TODO: Make this a mod
public class BasicWorldDataService implements ServerWorldDataService {
    public static final int WATER_LEVEL = 4;
    private final Noise2D continentalnessNoise;
    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;
    private final Noise2D erosionNoise;
    private final Noise2D worldNoise;
    private final World world;
    private final BiomeService biomeService;
    private final ServerBlock air = new ServerBlock("air");
    private final ServerBlock water = new ServerBlock("core:water_source_block");
    private final BlockService blockService = new BlockService();

    public BasicWorldDataService(Random random, World world) {
        this.continentalnessNoise = new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.0001);
        this.temperatureNoise = new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.0001);
        this.humidityNoise = new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.erosionNoise = new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.worldNoise = new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 3, 0.25, 2.5, 0.001);
        this.world = world;
        this.biomeService = new BiomeService(
                Map.of(
                        new ClimateVector(0.0, 0.82, 0.18, 0.0),
                        new DesertBiome(blockService),
                        new ClimateVector(0.0, 0.82, 0.82, 0.0),
                        new JungleBiome(blockService),
                        new ClimateVector(0.0, 0.18, 0.18, 0.0),
                        new TundraBiome(blockService),
                        new ClimateVector(0.0, 0.18, 0.82, 0.0),
                        new TaigaBiome(blockService),
                        new ClimateVector(0.0, 0.5, 0.5, 0.0),
                        new PlainsBiome(blockService)
                )
        );
    }

    @Override
    public @NotNull Block getBlockAt(int x, int y, int z, ClimateVector climateVector2D) {
        Position3D position3D = new Position3D(x, y, z);
        if (world.isBlockQueued(position3D)) {
            Block block = world.takeQueuedBlock(position3D);
            if (block != null) {
                return block;
            }
        }

        Biome biome = biomeService.generateBiome(climateVector2D);

        int height = (int) (
                (worldNoise.generate(x, z) - 0.5) * 1024
        );

        Block block = null;
        if (y <= height) {
            block = biome.getBlock(height - y, blockService);
        }
        if (block == null) {
            if (y <= WATER_LEVEL) {
                return water;
            }
            return air;
        }
        if (block instanceof StructureSeed(omnivoxel.server.client.structure.Structure structure)) {
            Block[][][] blocks = structure.getBlocks();
            int xl = blocks.length;
            int yl = blocks[0].length;
            int zl = blocks[0][0].length;
            for (int X = 0; X < xl; X++) {
                for (int Z = 0; Z < zl; Z++) {
                    for (int Y = 0; Y < yl; Y++) {
                        world.setBlock(position3D.add(X, Y, Z), blocks[X][Y][Z]);
                    }
                }
            }
        }
        return block;
    }

    @Override
    public @NotNull ClimateVector getClimateVector2D(int x, int z) {
        double continentalness = continentalnessNoise.generate(x, z);
        double temperature = temperatureNoise.generate(x, z) * (1 - continentalness);
        double humidity = humidityNoise.generate(x, z);
        double erosion = erosionNoise.generate(x, z);
        return new ClimateVector(continentalness, temperature, humidity, erosion);
    }
}
