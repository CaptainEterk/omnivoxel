package omnivoxel.server.client.chunk.worldDataService;

import core.noise._2D.FractionalBrownianNoise2D;
import core.noise._2D.PerlinNoise;
import core.noise._3D.FractionalBrownianNoise3D;
import core.noise._3D.PerlinNoise3D;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.ServerWorld;
import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import omnivoxel.util.math.Position3D;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

// TODO: Make this a mod
public class BasicWorldDataService implements ServerWorldDataService {
    public static final int WATER_LEVEL = 0;
    public static final int BIAS = 256;
    public static final int BASE = 128;

    private final Noise2D continentalnessNoise;
    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;
    private final Noise2D erosionNoise;
    private final Noise2D pvNoise;
    private final Noise3D worldNoise;
    private final ServerWorld world;
    private final BiomeService biomeService;
    private final ServerBlock air;
    private final ServerBlock water;
    private final ServerBlock stone;
    private final ServerBlockService blockService;
    private final Map<Position3D, PriorityServerBlock> queuedBlocks;

    public BasicWorldDataService(Random random, ServerWorld world, BiomeService biomeService, ServerBlockService blockService, Map<Position3D, PriorityServerBlock> queuedBlocks) {
        this.continentalnessNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.temperatureNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.0001);
        this.humidityNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.0001);
        this.erosionNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.pvNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.worldNoise = new FractionalBrownianNoise3D(new PerlinNoise3D(random.nextLong()), 4, 0.4, 2.5, 0.005);
        this.world = world;
        this.biomeService = biomeService;
        this.blockService = blockService;
        this.queuedBlocks = queuedBlocks;
        this.air = blockService.getBlock("omnivoxel:air");
        this.water = blockService.getBlock("core:water_source_block");
        this.stone = blockService.getBlock("core:stone_block");
    }

    @Override
    public void queueBlock(Position3D position3D, PriorityServerBlock block) {
        queuedBlocks.put(position3D, block);
    }

    @Override
    public @NotNull ServerBlock getBlockAt(Position3D chunkPosition, int x, int y, int z, int worldX, int worldY, int worldZ, boolean border, ChunkInfo chunkInfo) {
        Position3D position3D = new Position3D(worldX, worldY, worldZ);

        PriorityServerBlock priorityBlock = border ? queuedBlocks.get(position3D) : queuedBlocks.remove(position3D);
        if (priorityBlock != null && priorityBlock.priority().canOverwrite(PriorityServerBlock.Priority.WORLD_TERRAIN) && priorityBlock.serverBlock() != null) {
            return priorityBlock.serverBlock();
        }

        int height = chunkInfo.get(0, int[][].class)[x + 1][z + 1];
        ClimateVector climateVector2D = chunkInfo.get(1, ClimateVector[][].class)[x + 1][z + 1];
        ClimateVector climateVector3D = chunkInfo.get(2, ClimateVector[][][].class)[x + 1][z + 1][y + 1];
        Biome biome = chunkInfo.get(3, Biome[][][].class)[x + 1][z + 1][y + 1];

        boolean isBlock = isBlock(climateVector3D.get(0));
        int yOffset = worldY - height;
        double depth = (-yOffset) / 128.0;
        boolean isFloor = y >= ConstantGameSettings.CHUNK_HEIGHT ? isBlock(worldFunction(worldNoise.generate(worldX, worldY + 1, z), worldY, climateVector2D.get(0), climateVector2D.get(3), climateVector2D.get(4))) : isBlock(chunkInfo.get(2, ClimateVector[][][].class)[x + 1][z + 1][y + 2].get(0));
        boolean isCeiling = y < 0 ? isBlock(worldFunction(worldNoise.generate(worldX, worldY - 1, z), worldY, climateVector2D.get(0), climateVector2D.get(3), climateVector2D.get(4))) : isBlock(chunkInfo.get(2, ClimateVector[][][].class)[x + 1][z + 1][y].get(0));

        ServerBlock block;
        if (isBlock) {
            block = biome.getBlock(worldX, worldY, worldZ, yOffset, blockService);
            if (block == null) {
                block = stone;
            }
        } else {
            block = air;
        }

        if (priorityBlock != null && block == air && priorityBlock.serverBlock() != null) {
            return priorityBlock.serverBlock();
        }
        return block;
    }

    private boolean isBlock(double d) {
        return d > 0.5;
    }

    private int getHeight(int x, int z, ClimateVector climateVector2D) {
        for (int y = 319; y >= -64; y -= 4) {
            if (isBlock(worldFunction(worldNoise.generate(x, y, z), y, climateVector2D.get(0), climateVector2D.get(3), climateVector2D.get(4)))) {
                for (int o = 3; o > 0; o--) {
                    if (isBlock(worldFunction(worldNoise.generate(x, y + o, z), y + o, climateVector2D.get(0), climateVector2D.get(3), climateVector2D.get(4)))) {
                        return y + o;
                    }
                }
                return y;
            }
        }

        return Integer.MAX_VALUE;
    }

    @Override
    public ClimateVector getClimateVector2D(int x, int z) {
        double continentalness = continentalnessNoise.generate(x, z);
        double temperature = temperatureNoise.generate(x, z);// * (1 - (continentalness + 1) / 2);
        double humidity = humidityNoise.generate(x, z);
        double erosion = erosionNoise.generate(x, z);
        double pv = pvNoise.generate(x, z);
        return new ClimateVector(continentalness, temperature, humidity, erosion, pv);
    }

    @Override
    public ClimateVector getClimateVector3D(int x, int y, int z, ClimateVector climateVector2D) {
        return new ClimateVector(worldFunction(worldNoise.generate(x, y, z), y, climateVector2D.get(0), climateVector2D.get(3), climateVector2D.get(4)));
    }

    private double worldFunction(
            double world,
            int y,
            double continentalness,
            double erosion,
            double pv
    ) {
        return world / (y/128.0+1);
    }

    @Override
    public boolean shouldGenerateChunk(Position3D position3D) {
        return position3D.y() >= -2 && position3D.y() <= 10;
    }

    public Biome getBiome(int x, int y, int z, int height, ClimateVector climateVector2D) {
        int yOffset = y - height;
        double depth = (-yOffset) / 128.0;

        return biomeService.generateBiome(new ClimateVector(
                climateVector2D.get(0),
                climateVector2D.get(1),
                climateVector2D.get(2),
                climateVector2D.get(3),
                climateVector2D.get(4),
                depth
        ));
    }

    @Override
    public ChunkInfo getChunkInfo(Position3D position3D) {
        int[][] heights = new int[ConstantGameSettings.PADDED_WIDTH][ConstantGameSettings.PADDED_LENGTH];
        ClimateVector[][] climateVector2Ds = new ClimateVector[ConstantGameSettings.PADDED_WIDTH][ConstantGameSettings.PADDED_LENGTH];
        ClimateVector[][][] climateVector3Ds = new ClimateVector[ConstantGameSettings.PADDED_WIDTH][ConstantGameSettings.PADDED_LENGTH][ConstantGameSettings.PADDED_HEIGHT];
        Biome[][][] biomes = new Biome[ConstantGameSettings.PADDED_WIDTH][ConstantGameSettings.PADDED_LENGTH][ConstantGameSettings.PADDED_HEIGHT];
        for (int x = -1; x <= ConstantGameSettings.CHUNK_WIDTH; x++) {
            int worldX = position3D.x() * ConstantGameSettings.CHUNK_WIDTH + x;
            for (int z = -1; z <= ConstantGameSettings.CHUNK_LENGTH; z++) {
                int worldZ = position3D.z() * ConstantGameSettings.CHUNK_LENGTH + z;
                ClimateVector climateVector2D = getClimateVector2D(worldX, worldZ);
                int height = getHeight(worldX, worldZ, climateVector2D);
                heights[x + 1][z + 1] = height;
                climateVector2Ds[x + 1][z + 1] = climateVector2D;
                for (int y = -1; y <= ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    int worldY = position3D.y() * ConstantGameSettings.CHUNK_HEIGHT + y;
                    climateVector3Ds[x + 1][z + 1][y + 1] = getClimateVector3D(worldX, worldY, worldZ, climateVector2D);
                    biomes[x + 1][z + 1][y + 1] = getBiome(worldX, worldY, worldZ, height, climateVector2D);
                }
            }
        }
        return new ChunkInfo(heights, climateVector2Ds, climateVector3Ds, biomes);
    }
}