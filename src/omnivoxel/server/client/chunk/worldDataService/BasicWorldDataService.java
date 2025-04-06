package omnivoxel.server.client.chunk.worldDataService;

import core.biomes.*;
import core.noise._2D.FractionalBrownianNoise2D;
import core.noise._2D.PerlinNoise;
import core.noise._3D.FractionalBrownianNoise3D;
import core.noise._3D.PerlinNoise3D;
import core.structures.TestStructure;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.BiomeService;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import omnivoxel.server.client.structure.Structure;
import omnivoxel.server.client.structure.StructureBoundingBox;
import omnivoxel.server.client.structure.StructureSeed;
import omnivoxel.server.client.structure.StructureService;
import omnivoxel.server.world.World;
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
    private final World world;
    private final BiomeService biomeService;
    private final ServerBlock air;
    private final ServerBlock water;
    private final ServerBlock stone;
    private final BlockService blockService = new BlockService();
    private final StructureService structureService;

    public BasicWorldDataService(Random random, World world) {
        this.continentalnessNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.temperatureNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.0001);
        this.humidityNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.erosionNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 2, 0.25, 2.5, 0.001);
        this.ridgesNoise = new FractionalBrownianNoise2D(new PerlinNoise(random.nextLong()), 5, 0.4, 2.5, 0.0025);
        this.depthNoise = new FractionalBrownianNoise3D(new PerlinNoise3D(random.nextLong()), 1, 100, 2, 0.01);
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
        this.air = blockService.getBlock("omnivoxel:air", null);
        this.water = blockService.getBlock("core:water_source_block", null);
        this.stone = blockService.getBlock("core:stone_block", null);
        structureService = new StructureService();
        structureService.register(new TestStructure().initBlocks(blockService));
    }

    @Override
    public @NotNull Block getBlockAt(ChunkPosition chunkPosition, int x, int y, int z, ClimateVector climateVector2D) {
        Position3D position3D = new Position3D(x, y, z);

        Block block = world.takeQueuedBlock(position3D);
        if (block != null) {
            return block;
        }

        double depth = depthNoise.generate(x, y, z);

        Biome biome = biomeService.generateBiome(new ClimateVector(climateVector2D));

        int height = (int) (
                (1 - Math.abs(3 * Math.abs(ridgesNoise.generate(x, z)) - 2)) * 256
//                continentalnessNoise.generate(x, z) * 128
        );

        StructureSeed structureSeed = structureService.getStructure(biome, x, y, z, climateVector2D, new ClimateVector(height, depth));
        if (structureSeed == null) {
            if (y <= height) {
                block = biome.getBlock(x, y, z, height - y, blockService);
            } else {
                if (y <= WATER_LEVEL) {
                    return water;
                }
                return air;
            }
        } else {
            Structure structure = structureSeed.structure();
            Map<Position3D, ServerBlock> blocks = structure.getBlocks();
            StructureBoundingBox boundingBox = structure.getBoundingBox();
            Position3D origin = position3D.add(structure.getOrigin()).add(structureSeed.offset());
            int xl = boundingBox.getWidth();
            int yl = boundingBox.getHeight();
            int zl = boundingBox.getLength();
            for (int X = 0; X < xl; X++) {
                for (int Z = 0; Z < zl; Z++) {
                    for (int Y = 0; Y < yl; Y++) {
                        world.setBlock(origin.add(X, Y, Z), blocks.get(new Position3D(X, Y, Z)));
                    }
                }
            }
            block = blocks.get(structure.getOrigin());
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