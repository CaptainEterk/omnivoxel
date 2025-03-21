package omnivoxel.server.client.chunk.worldDataService;

import core.biomes.*;
import core.noise.FractionalBrownianNoise;
import core.noise.PerlinNoise;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.block.StructureSeed;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.worldDataService.noise.CachedNoiseWrapper;
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

    public BasicWorldDataService(Random random, World world) {
        this.continentalnessNoise = new CachedNoiseWrapper(new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 3, 0.25, 2.5, 0.0001));
        this.temperatureNoise = new CachedNoiseWrapper(new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 3, 0.25, 2.5, 0.0001));
        this.humidityNoise = new CachedNoiseWrapper(new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 3, 0.25, 2.5, 0.001));
        this.erosionNoise = new CachedNoiseWrapper(new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 3, 0.25, 2.5, 0.001));
        this.worldNoise = new CachedNoiseWrapper(new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 3, 0.25, 2.5, 0.001));
        this.world = world;
        this.biomeService = new BiomeService(
                Map.of(
                        new ClimateVector(0.0, 0.82, 0.18, 0.0),
                        new DesertBiome(),
                        new ClimateVector(0.0, 0.82, 0.82, 0.0),
                        new JungleBiome(),
                        new ClimateVector(0.0, 0.18, 0.18, 0.0),
                        new TundraBiome(),
                        new ClimateVector(0.0, 0.18, 0.82, 0.0),
                        new TaigaBiome(),
                        new ClimateVector(0.0, 0.5, 0.5, 0.0),
                        new PlainsBiome()
                )
        );
    }

    @Override
    public @NotNull Block getBlockAt(ChunkPosition chunkPosition, int x, int y, int z) {
        long worldX = (long) chunkPosition.x() * ConstantGameSettings.CHUNK_WIDTH + x;
        long worldY = (long) chunkPosition.y() * ConstantGameSettings.CHUNK_HEIGHT + y;
        long worldZ = (long) chunkPosition.z() * ConstantGameSettings.CHUNK_LENGTH + z;

        Position3D position3D = new Position3D(worldX, worldY, worldZ);
        if (world.isBlockQueued(position3D)) {
            Block block = world.takeQueuedBlock(position3D);
            if (block != null) {
                return block;
            }
        }

        double continentalness = continentalnessNoise.generate(worldX, worldZ);
        double temperature = temperatureNoise.generate(worldX, worldZ) * (1 - continentalness);
        double humidity = humidityNoise.generate(worldX, worldZ);
        double erosion = erosionNoise.generate(worldX, worldZ);
        Biome biome = biomeService.generateBiome(new ClimateVector(continentalness, temperature, humidity, erosion));

        int height = (int) (
                (worldNoise.generate(worldX, worldZ) - 0.5) * 32 * 32
        );

        int yPosition = chunkPosition.y() * ConstantGameSettings.CHUNK_HEIGHT + y;

        Block block = null;
//        if (yPosition <= height) {
//            if (yPosition > height - 2) {
//                if (height - yPosition == 0) {
//                    block = "core:grass_block";
//                } else {
//                    block = "core:dirt_block";
//                }
//            } else {
//                block = "core:stone_block";
//            }
//        } else if (yPosition < WATER_LEVEL) {
//            block = "core:water_source_block";
//        }
        if (yPosition <= height) {
            block = biome.getBlock(height - yPosition);
        }
        if (block == null) {
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
//                        queuedBlocks.put(position3D.add(X, Y, Z), blocks[X][Y][Z]);
                    }
                }
            }
        }
        return block;
//        if (yPosition <= height) {
//            return new ServerBlock("core:debug_climate", (int) (temperature * 16), (int) (continentalness * 16), (int) (humidity * 16));
//        } else {
//            return new ServerBlock("air");
//        }
    }
}
