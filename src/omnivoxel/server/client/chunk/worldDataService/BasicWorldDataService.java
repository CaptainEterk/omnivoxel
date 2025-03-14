package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.thread.mesh.util.noise.FractionalBrownianNoise;
import omnivoxel.server.client.block.ServerBlock;

// TODO: Make this a mod
public class BasicWorldDataService implements ServerWorldDataService {
    public static final int WATER_LEVEL = 4;
    private final FractionalBrownianNoise worldNoise;
    private final FractionalBrownianNoise temperatureNoise;
    private final FractionalBrownianNoise humidityNoise;
    private final FractionalBrownianNoise continentalnessNoise;

    // TODO: Make this not take in any inputs
    public BasicWorldDataService(FractionalBrownianNoise worldNoise, FractionalBrownianNoise temperatureNoise, FractionalBrownianNoise humidityNoise, FractionalBrownianNoise continentalnessNoise) {
        this.worldNoise = worldNoise;
        this.temperatureNoise = temperatureNoise;
        this.humidityNoise = humidityNoise;
        this.continentalnessNoise = continentalnessNoise;
    }

    @Override
    public ServerBlock getBlockAt(ChunkPosition chunkPosition, int x, int y, int z) {
        double continentalness = continentalnessNoise.generate(
                chunkPosition.x() * ConstantGameSettings.CHUNK_WIDTH + x,
                chunkPosition.z() * ConstantGameSettings.CHUNK_LENGTH + z
        );
        int height = (int) (
                worldNoise.generate(
                        chunkPosition.x() * ConstantGameSettings.CHUNK_WIDTH + x,
                        chunkPosition.z() * ConstantGameSettings.CHUNK_LENGTH + z
                ) * 32 * 16
        );
        double temperature = temperatureNoise.generate(
                chunkPosition.x() * ConstantGameSettings.CHUNK_WIDTH + x,
                chunkPosition.z() * ConstantGameSettings.CHUNK_LENGTH + z
        );
        double humidity = humidityNoise.generate(
                chunkPosition.x() * ConstantGameSettings.CHUNK_WIDTH + x,
                chunkPosition.z() * ConstantGameSettings.CHUNK_LENGTH + z
        );
        int yPosition = chunkPosition.y() * ConstantGameSettings.CHUNK_HEIGHT + y;
        String block = null;
        if (yPosition <= height) {
            if (yPosition > height - 2) {
                if (height - yPosition == 0) {
                    block = "core:grass_block";
                } else {
                    block = "core:dirt_block";
                }
            } else {
                block = "core:stone_block";
            }
        } else if (yPosition < WATER_LEVEL) {
            block = "core:water_source_block";
        }
        return new ServerBlock(block);
//        if (yPosition <= height) {
//            return new ServerBlock("core:debug_climate", (int) (temperature * 16), (int) (continentalness * 16), (int) (humidity * 16));
//        } else {
//            return new ServerBlock("air");
//        }
    }
}
