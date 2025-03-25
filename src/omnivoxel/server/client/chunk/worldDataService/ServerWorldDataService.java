package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import org.jetbrains.annotations.NotNull;

public interface ServerWorldDataService {
    @NotNull Block getBlockAt(int x, int y, int z, ClimateVector climateVector2D);

    @NotNull ClimateVector getClimateVector2D(int x, int z);
}