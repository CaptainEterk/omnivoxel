package omnivoxel.server.client.chunk;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.world.chunk.result.GeneratedChunk;

public class ChunkGenerator {
    private final ServerWorldDataService worldDataService;

    public ChunkGenerator(ServerWorldDataService worldDataService) {
        this.worldDataService = worldDataService;
    }

    public GeneratedChunk generateChunk(ChunkPosition chunkPosition) {
        GeneratedChunk chunk = new GeneratedChunk();
        for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
            for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
                int worldX = chunkPosition.x() * ConstantGameSettings.CHUNK_WIDTH + x;
                int worldZ = chunkPosition.z() * ConstantGameSettings.CHUNK_LENGTH + z;

                ClimateVector climateVector2D = worldDataService.getClimateVector2D(worldX, worldZ);
                for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {
                    int worldY = chunkPosition.y() * ConstantGameSettings.CHUNK_HEIGHT + y;
                    chunk.setBlock(x, y, z, worldDataService.getBlockAt(worldX, worldY, worldZ, climateVector2D));
                }
            }
        }
        return chunk;
    }
}