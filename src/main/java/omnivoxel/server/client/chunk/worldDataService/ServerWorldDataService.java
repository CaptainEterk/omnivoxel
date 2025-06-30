package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.util.math.Position3D;
import org.jetbrains.annotations.NotNull;

public interface ServerWorldDataService {
    @NotNull ServerBlock getBlockAt(Position3D position3D, int x, int y, int z, int worldX, int worldY, int worldZ, boolean border, ChunkInfo chunkInfo);

    ClimateVector getClimateVector2D(int x, int z);

    ClimateVector getClimateVector3D(int x, int y, int z, ClimateVector climateVector2D);

    boolean shouldGenerateChunk(Position3D position3D);

    void queueBlock(Position3D position3D, PriorityServerBlock block);

    ChunkInfo getChunkInfo(Position3D position3D);
}