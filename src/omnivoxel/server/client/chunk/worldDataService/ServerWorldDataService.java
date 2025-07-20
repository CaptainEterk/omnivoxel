package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.functions.Noise3DDensityFunction;
import omnivoxel.util.math.Position3D;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

public final class ServerWorldDataService {
    private final ServerBlockService blockService;
    private final DensityFunction generator;

    public ServerWorldDataService(ServerBlockService blockService, Value worldGenerator) {
        this.blockService = blockService;
        generator = getGenerator(worldGenerator);
    }

    private static DensityFunction getGenerator(Value worldGenerator) {
        String type = worldGenerator.getMember("type").as(String.class);
        Value[] args = worldGenerator.hasMember("args") ? worldGenerator.getMember("args").as(Value[].class) : new Value[0];
        switch (type) {
            case "noise3d":
                return new Noise3DDensityFunction(args);
            default:
                throw new IllegalArgumentException(String.format("%s is not a valid type for a density function", type));
        }
    }

    @NotNull
    public ServerBlock getBlockAt(Position3D position3D, int x, int y, int z, int worldX, int worldY, int worldZ, boolean border, ChunkInfo chunkInfo) {
        return generator.evaluate(worldX, worldY, worldZ) > 0 ? blockService.getBlock("core:stone_block") : blockService.getBlock("omnivoxel:air");
    }

    public ChunkInfo getChunkInfo(Position3D position3D) {
        return null;
    }
}