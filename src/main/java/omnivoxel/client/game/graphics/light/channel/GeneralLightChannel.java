package omnivoxel.client.game.graphics.light.channel;

import omnivoxel.common.settings.ConstantCommonSettings;

public class GeneralLightChannel implements LightChannel {
    private final int[] channel;

    public GeneralLightChannel(int[] channel) {
        this.channel = channel;
    }

    public GeneralLightChannel(LightChannel lightChannel) {
        channel = new int[(ConstantCommonSettings.BLOCKS_IN_CHUNK + 7) >> 3];

        for (int i = 0; i < ConstantCommonSettings.BLOCKS_IN_CHUNK; i++) {
            setLighting(i, lightChannel.getLighting(i));
        }
    }

    @Override
    public byte getLighting(int index) {
        int arrayIndex = index >> 3;
        int shift = (index & 7) << 2;

        return (byte) ((channel[arrayIndex] >>> shift) & 0xF);
    }

    @Override
    public LightChannel setLighting(int index, byte newLight) {
        int arrayIndex = index >> 3;
        int shift = (index & 7) << 2;

        channel[arrayIndex] =
                (channel[arrayIndex] & ~(0xF << shift))
                        | ((newLight & 0xF) << shift);

        return this;
    }
}