package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import org.jetbrains.annotations.NotNull;

public interface ServerWorldDataService {
    @NotNull Block getBlockAt(ChunkPosition chunkPosition, int x, int y, int z, ClimateVector climateVector2D);

    @NotNull ClimateVector getClimateVector2D(int x, int z);
}