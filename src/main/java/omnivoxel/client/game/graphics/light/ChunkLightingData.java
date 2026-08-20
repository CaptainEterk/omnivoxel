package omnivoxel.client.game.graphics.light;

import omnivoxel.client.game.graphics.light.channel.LightChannel;
import omnivoxel.client.game.graphics.light.channel.LightChannels;

public final class ChunkLightingData {
    private final Object lock = new Object();

    private LightChannel redChannel;
    private LightChannel greenChannel;
    private LightChannel blueChannel;
    private LightChannel skylightChannel;

    public ChunkLightingData(
            LightChannel redChannel,
            LightChannel greenChannel,
            LightChannel blueChannel,
            LightChannel skylightChannel
    ) {
        this.redChannel = redChannel;
        this.greenChannel = greenChannel;
        this.blueChannel = blueChannel;
        this.skylightChannel = skylightChannel;
    }

    public LightChannel getChannel(LightChannels channel) {
        synchronized (lock) {
            return switch (channel) {
                case RED -> redChannel;
                case GREEN -> greenChannel;
                case BLUE -> blueChannel;
                case SKYLIGHT -> skylightChannel;
            };
        }
    }

    public void setChannel(LightChannels channel, LightChannel lightChannel) {
        synchronized (lock) {
            switch (channel) {
                case RED -> redChannel = lightChannel;
                case GREEN -> greenChannel = lightChannel;
                case BLUE -> blueChannel = lightChannel;
                case SKYLIGHT -> skylightChannel = lightChannel;
            }
        }
    }

    public boolean isComplete() {
        synchronized (lock) {
            return redChannel != null
                    && greenChannel != null
                    && blueChannel != null
                    && skylightChannel != null;
        }
    }
}