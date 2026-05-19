package omnivoxel.client.game.graphics.light;

import omnivoxel.client.game.graphics.light.channel.LightChannel;
import omnivoxel.client.game.graphics.light.channel.LightChannels;

public record ChunkLightingData(
        LightChannel redChannel,
        LightChannel greenChannel,
        LightChannel blueChannel,
        LightChannel skylightChannel
) {
    public LightChannel getChannel(LightChannels channel) {
        return switch (channel) {
            case LightChannels.RED -> redChannel;
            case LightChannels.GREEN -> greenChannel;
            case LightChannels.BLUE -> blueChannel;
            case LightChannels.SKYLIGHT -> skylightChannel;
        };
    }
}