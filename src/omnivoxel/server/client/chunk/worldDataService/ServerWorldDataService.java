package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import omnivoxel.server.client.chunk.worldDataService.density.functions.*;
import omnivoxel.util.math.Position3D;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ServerWorldDataService {
    private static final Map<String, Class<? extends DensityFunction>> densityCache = new HashMap<>();
    private final ServerBlockService blockService;
    private final DensityFunction generator;
    private final Integer chunkMinX;
    private final Integer chunkMinY;
    private final Integer chunkMinZ;
    private final Integer chunkMaxX;
    private final Integer chunkMaxY;
    private final Integer chunkMaxZ;

    public ServerWorldDataService(ServerBlockService blockService, Value worldGenerator) {
        this.blockService = blockService;

        addDensityFunction(Noise3DDensityFunction.class);
        addDensityFunction(ValueDensityFunction.class);

        addDensityFunction(XClampedGradientDensityFunction.class);
        addDensityFunction(YClampedGradientDensityFunction.class);
        addDensityFunction(ZClampedGradientDensityFunction.class);

        addDensityFunction(XDensityFunction.class);
        addDensityFunction(YDensityFunction.class);
        addDensityFunction(ZDensityFunction.class);

        addDensityFunction(AddDensityFunction.class);
        addDensityFunction(MulDensityFunction.class);
        addDensityFunction(MinDensityFunction.class);
        addDensityFunction(MaxDensityFunction.class);
        addDensityFunction(AbsDensityFunction.class);
        addDensityFunction(RangeChoiceDensityFunction.class);
        addDensityFunction(InterpolatedDensityFunction.class);
        addDensityFunction(SqueezeDensityFunction.class);
        addDensityFunction(QuarterNegativeDensityFunction.class);

        chunkMinX = worldGenerator.hasMember("chunk_min_x") ? worldGenerator.getMember("chunk_min_x").as(Integer.class) : null;
        chunkMinY = worldGenerator.hasMember("chunk_min_y") ? worldGenerator.getMember("chunk_min_y").as(Integer.class) : null;
        chunkMinZ = worldGenerator.hasMember("chunk_min_z") ? worldGenerator.getMember("chunk_min_z").as(Integer.class) : null;

        chunkMaxX = worldGenerator.hasMember("chunk_max_x") ? worldGenerator.getMember("chunk_max_x").as(Integer.class) : null;
        chunkMaxY = worldGenerator.hasMember("chunk_max_y") ? worldGenerator.getMember("chunk_max_y").as(Integer.class) : null;
        chunkMaxZ = worldGenerator.hasMember("chunk_max_z") ? worldGenerator.getMember("chunk_max_z").as(Integer.class) : null;

        generator = getGenerator(worldGenerator.getMember("final_density"), 100L);
    }

    public static DensityFunction getGenerator(Value value, long seed) {
        try {
            if (value.isNumber()) {
                return new ValueDensityFunction(null, Double.doubleToLongBits(value.as(Double.class)));
            }
            String type = value.getMember("type").as(String.class);
            Value[] args = value.hasMember("args") ? value.getMember("args").as(Value[].class) : new Value[0];
            Class<? extends DensityFunction> dfClass = densityCache.get(type);
            if (dfClass == null) {
                throw new IllegalArgumentException(String.format("%s is not a valid type for a density function", type));
            }
            return dfClass.getConstructor(Value[].class, long.class).newInstance(args, seed);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDensityFunction(Class<? extends DensityFunction> densityFunction) {
        Function function = densityFunction.getAnnotationsByType(Function.class)[0];
        String id = function.id();
        densityCache.put(id, densityFunction);
    }

    public boolean shouldGenerateChunk(Position3D position3D) {
        boolean withinX = (chunkMinX == null || chunkMaxX == null) ||
                (position3D.x() >= chunkMinX && position3D.x() <= chunkMaxX);
        boolean withinY = (chunkMinY == null || chunkMaxY == null) ||
                (position3D.y() >= chunkMinY && position3D.y() <= chunkMaxY);
        boolean withinZ = (chunkMinZ == null || chunkMaxZ == null) ||
                (position3D.z() >= chunkMinZ && position3D.z() <= chunkMaxZ);

        return withinX && withinY && withinZ;
    }

    @NotNull
    public ServerBlock getBlockAt(Position3D position3D, int x, int y, int z, int worldX, int worldY, int worldZ, boolean border, ChunkInfo chunkInfo) {
        ServerBlock serverBlock = generator.evaluate(worldX, worldY, worldZ) > 0 ? blockService.getBlock("core:stone_block") : blockService.getBlock("omnivoxel:air");
        if (worldY < 100 && !Objects.equals(serverBlock.id(), "core:stone_block")) {
            return blockService.getBlock("core:water_source_block");
        }
        return serverBlock;
    }

    public ChunkInfo getChunkInfo(Position3D position3D) {
        return null;
    }
}