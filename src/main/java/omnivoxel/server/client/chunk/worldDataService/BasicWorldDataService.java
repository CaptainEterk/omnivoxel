package omnivoxel.server.client.chunk.worldDataService;

import core.noise._2D.FractionalBrownianNoise2D;
import core.noise._2D.PerlinNoise;
import core.noise._3D.FractionalBrownianNoise3D;
import core.noise._3D.PerlinNoise3D;
import omnivoxel.math.Position3D;
import omnivoxel.server.ServerWorld;
import omnivoxel.server.client.ServerItem;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

// TODO: Make this a mod
public class BasicWorldDataService implements ServerWorldDataService {
    public static final int WATER_LEVEL = 0;
    private final Noise2D continentalnessNoise;
    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;
    private final Noise2D erosionNoise;
    private final Noise2D ridgesNoise;
    private final Noise3D depthNoise;
    private final ServerWorld world;
    private final BiomeService biomeService;
    private final ServerBlock air;
    private final ServerBlock water;
    private final ServerBlock underwater;
    private final BlockService blockService;
    private final Map<Position3D, ServerBlock> queuedBlocks;

    public BasicWorldDataService(Random random, ServerWorld world, BiomeService biomeService, BlockService blockService, Map<Position3D, ServerBlock> queuedBlocks) {
        this.continentalnessNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.temperatureNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.0001);
        this.humidityNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.erosionNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.ridgesNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 5, 0.4, 2.5, 0.0025);
        this.depthNoise = new FractionalBrownianNoise3D(new PerlinNoise3D(random.nextLong()), 1, 100, 2, 0.01);
        this.world = world;
        this.biomeService = biomeService;
        this.blockService = blockService;
        this.queuedBlocks = queuedBlocks;
        this.air = blockService.getBlock("omnivoxel:air", null);
        this.water = blockService.getBlock("core:water_source_block", new int[]{1});
        this.underwater = blockService.getBlock("core:water_source_block", new int[]{0});
    }

    @Override
    public void queueBlock(Position3D position3D, ServerBlock block) {
        queuedBlocks.put(position3D, block);
    }

    @Override
    public @NotNull ServerBlock getBlockAt(Position3D chunkPosition, int x, int y, int z, ClimateVector climateVector2D) {
//        if (true) {
//            return y < 1 ? stone : air;
//        }

        Position3D position3D = new Position3D(x, y, z);

        ServerItem block;// = queuedBlocks.remove(position3D);
//        if (block instanceof ServerBlock serverBlock) {
//            return serverBlock;
//        }

        Biome biome = biomeService.generateBiome(new ClimateVector(climateVector2D));
        int height = (int) climateVector2D.get(0);

        if (y <= height) {
            block = biome.getBlock(x, y, z, height - y, blockService);
        } else {
            return y <= WATER_LEVEL ? water : air;
        }

        return (ServerBlock) block;
//        return new ServerBlock(((ServerBlock) block).id(), new int[]{(int) climateVector2D.get(1) * 32, 0, 0});
    }

    @Override
    public ClimateVector getClimateVector2D(int x, int z) {
        double continentalness = continentalnessNoise.generate(x, z);
        double temperature = temperatureNoise.generate(x, z);// * (1 - (continentalness + 1) / 2);
        double humidity = humidityNoise.generate(x, z);
        double erosion = erosionNoise.generate(x, z);
        double height = (1 - Math.abs(3 * Math.abs(ridgesNoise.generate(x, z)) - 2)) * 256;
        return new ClimateVector(height, continentalness, temperature, humidity, erosion);
    }

    @Override
    public ClimateVector getClimateVector3D(int x, int y, int z) {
        double depth = depthNoise.generate(x, y, z);
        return new ClimateVector(depth);
    }

    @Override
    public boolean shouldGenerateChunk(Position3D position3D) {
        return Math.abs(position3D.y()) < 8;
//        return position3D.y() == 0;
    }
}