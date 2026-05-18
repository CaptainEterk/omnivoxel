package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.world.ServerWorld;
import omnivoxel.util.IndexCalculator;
import omnivoxel.util.math.Position2D;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.chunk2d.Chunk2D;
import omnivoxel.world.chunk2d.SingleBlockChunk2D;

public final class ServerWorldDataService {
    private static final int STEP = 2;
    private final ServerBlockService blockService;
    private final WorldGenerator worldGenerator;

    public ServerWorldDataService(ServerBlockService blockService, WorldGenerator worldGenerator) {
        this.blockService = blockService;
        this.worldGenerator = worldGenerator;
    }

    public boolean shouldGenerateChunk(Position3D position3D) {
        boolean withinX = (worldGenerator.getChunkMinX() == null || worldGenerator.getChunkMaxX() == null) ||
                (position3D.x() >= worldGenerator.getChunkMinX() && position3D.x() <= worldGenerator.getChunkMaxX());
        boolean withinY = (worldGenerator.getChunkMinY() == null || worldGenerator.getChunkMaxY() == null) ||
                (position3D.y() >= worldGenerator.getChunkMinY() && position3D.y() <= worldGenerator.getChunkMaxY());
        boolean withinZ = (worldGenerator.getChunkMinZ() == null || worldGenerator.getChunkMaxZ() == null) ||
                (position3D.z() >= worldGenerator.getChunkMinZ() && position3D.z() <= worldGenerator.getChunkMaxZ());

        return withinX && withinY && withinZ;
    }

    public boolean shouldGenerateBlock(int worldX, int worldY, int worldZ) {
        boolean withinX = (worldGenerator.getBlockMinX() == null || worldGenerator.getBlockMaxX() == null) ||
                (worldX >= worldGenerator.getBlockMinX() && worldX < worldGenerator.getBlockMaxX());
        boolean withinY = (worldGenerator.getBlockMinY() == null || worldGenerator.getBlockMaxY() == null) ||
                (worldY >= worldGenerator.getBlockMinY() && worldY < worldGenerator.getBlockMaxY());
        boolean withinZ = (worldGenerator.getBlockMinZ() == null || worldGenerator.getBlockMaxX() == null) ||
                (worldZ >= worldGenerator.getBlockMinZ() && worldZ < worldGenerator.getBlockMaxX());

        return withinX && withinY && withinZ;
    }

    @NotNull
    public ServerBlock getBlockAt(int x, int y, int z,
                                  int worldX, int worldY, int worldZ,
                                  ChunkInfo chunkInfo) {
        if (!shouldGenerateBlock(worldX, worldY, worldZ)) {
            return ServerBlock.AIR;
        }

        double density = chunkInfo.densityCache()[IndexCalculator.calculateBlockIndexPadded(x, y, z)];
        double ncFloor = chunkInfo.densityCache()[IndexCalculator.calculateBlockIndexPadded(x, y - 1, z)];
        double ncCeiling = chunkInfo.densityCache()[IndexCalculator.calculateBlockIndexPadded(x, y + 1, z)];

        boolean isFloor = ncFloor > 0;
        boolean isCeiling = ncCeiling > 0;

        String result = worldGenerator.getBlockFunction().evaluate(
                density, null,
                isFloor, isCeiling,
                chunkInfo.heights()[IndexCalculator.calculateBlockIndexPadded2D(x, z)] - worldY,
                worldX, worldY, worldZ
        );

        return blockService.getBlock(result);
    }

    public ChunkInfo getChunkInfo(ServerWorld world, Position3D position3D) {
        int chunkMinWorldY = position3D.y() * ConstantCommonSettings.CHUNK_HEIGHT;
        int chunkMaxWorldY = chunkMinWorldY + ConstantCommonSettings.CHUNK_HEIGHT - 1;

        int paddedX = ConstantCommonSettings.CHUNK_WIDTH + 2;
        int paddedY = ConstantCommonSettings.CHUNK_HEIGHT + 2;
        int paddedZ = ConstantCommonSettings.CHUNK_LENGTH + 2;

        int sx = Math.floorDiv(paddedX + STEP - 1, STEP) + 1;
        int sy = Math.floorDiv(paddedY + STEP - 1, STEP) + 1;
        int sz = Math.floorDiv(paddedZ + STEP - 1, STEP) + 1;

        double[] sparse = new double[sx * sy * sz];

        for (int x = -1; x <= ConstantCommonSettings.CHUNK_WIDTH; x += STEP) {
            int worldX = position3D.x() * ConstantCommonSettings.CHUNK_WIDTH + x;

            for (int z = -1; z <= ConstantCommonSettings.CHUNK_LENGTH; z += STEP) {
                int worldZ = position3D.z() * ConstantCommonSettings.CHUNK_LENGTH + z;

                for (int y = -1; y <= ConstantCommonSettings.CHUNK_HEIGHT; y += STEP) {
                    int worldY = position3D.y() * ConstantCommonSettings.CHUNK_HEIGHT + y;

                    int ix = (x + 1) / STEP;
                    int iy = (y + 1) / STEP;
                    int iz = (z + 1) / STEP;

                    int index = ix + sx * (iy + sy * iz);

                    sparse[index] = worldGenerator.getDensityFunction().evaluate(worldX, worldY, worldZ);
                }
            }
        }

        double[] densityCache = new double[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];

        for (int x = -1; x <= ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = -1; z <= ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = -1; y <= ConstantCommonSettings.CHUNK_HEIGHT; y++) {

                    int gx = (x + 1) / STEP;
                    int gy = (y + 1) / STEP;
                    int gz = (z + 1) / STEP;

                    double fx = ((x + 1) % STEP) / (double) STEP;
                    double fy = ((y + 1) % STEP) / (double) STEP;
                    double fz = ((z + 1) % STEP) / (double) STEP;

                    double c000 = sparse[gx + sx * (gy + sy * gz)];
                    double c100 = sparse[(gx + 1) + sx * (gy + sy * gz)];
                    int i = sx * ((gy + 1) + sy * gz);
                    double c010 = sparse[gx + i];
                    double c110 = sparse[(gx + 1) + i];
                    int i1 = sx * (gy + sy * (gz + 1));
                    double c001 = sparse[gx + i1];
                    double c101 = sparse[(gx + 1) + i1];
                    int i2 = sx * ((gy + 1) + sy * (gz + 1));
                    double c011 = sparse[gx + i2];
                    double c111 = sparse[(gx + 1) + i2];

                    double x00 = c000 + fx * (c100 - c000);
                    double x10 = c010 + fx * (c110 - c010);
                    double x01 = c001 + fx * (c101 - c001);
                    double x11 = c011 + fx * (c111 - c011);

                    double y0 = x00 + fy * (x10 - x00);
                    double y1 = x01 + fy * (x11 - x01);

                    double value = y0 + fz * (y1 - y0);

                    densityCache[IndexCalculator.calculateBlockIndexPadded(x, y, z)] = value;
                }
            }
        }

        Position2D position2D = position3D.getPosition2D();
        int[] heights = new int[ConstantCommonSettings.PADDED_WIDTH * ConstantCommonSettings.PADDED_LENGTH];
        Chunk2D<Integer> chunkHeights = world.getChunkHeights(position2D);
        boolean cachedHeights = chunkHeights != null;
        chunkHeights = cachedHeights ? chunkHeights : new SingleBlockChunk2D<>(0);
        if (worldGenerator.getChunkMaxY() != null && worldGenerator.getChunkMinY() != null) {
            for (int x = -1; x <= ConstantCommonSettings.CHUNK_WIDTH; x++) {
                int worldX = position3D.x() * ConstantCommonSettings.CHUNK_WIDTH + x;
                for (int z = -1; z <= ConstantCommonSettings.CHUNK_LENGTH; z++) {
                    int worldZ = position3D.z() * ConstantCommonSettings.CHUNK_LENGTH + z;
                    for (int worldY = worldGenerator.getBlockMaxY() ; worldY > worldGenerator.getBlockMinY() ; worldY--) {
                        boolean in = x >= 0 && x < ConstantCommonSettings.CHUNK_WIDTH && z >= 0 && z < ConstantCommonSettings.CHUNK_LENGTH;
                        double heightDensity;
                        if (cachedHeights && in) {
                            heightDensity = 1;
                            worldY = chunkHeights.getBlock(x, z);
                        } else if (worldGenerator.isHeightIsDensityFunction() && worldY > chunkMinWorldY && worldY < chunkMaxWorldY) {
                            heightDensity = densityCache[IndexCalculator.calculateBlockIndexPadded(x, worldY - chunkMinWorldY, z)];
                        } else {
                            heightDensity = worldGenerator.getHeightFunction().evaluate(worldX, worldY, worldZ);
                        }
                        if (heightDensity > 0) {
                            heights[IndexCalculator.calculateBlockIndexPadded2D(x, z)] = worldY;
                            if (!cachedHeights && in) {
                                chunkHeights = chunkHeights.setBlock(x, z, worldY);
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (!cachedHeights) {
            world.putChunkHeights(position2D, chunkHeights);
        }

        return new ChunkInfo(heights, densityCache);
    }

    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }
}