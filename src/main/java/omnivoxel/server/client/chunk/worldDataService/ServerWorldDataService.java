package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.math.Position3D;
import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import org.jetbrains.annotations.NotNull;

public interface ServerWorldDataService {
    @NotNull ServerBlock getBlockAt(Position3D position3D, int x, int y, int z, boolean border, ClimateVector climateVector2D);

    ClimateVector getClimateVector2D(int x, int z);

    ClimateVector getClimateVector3D(int x, int y, int z);

    boolean shouldGenerateChunk(Position3D position3D);

    void queueBlock(Position3D position3D, PriorityServerBlock block);
}