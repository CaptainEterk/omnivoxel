package omnivoxel.client.game.graphics.block;

import omnivoxel.client.game.graphics.light.channel.LightChannels;
import omnivoxel.common.block.shape.BlockShape;
import omnivoxel.common.block.hitbox.BlockHitbox;
import omnivoxel.common.face.BlockFace;

public abstract class BlockMesh {
    protected final String state;

    protected BlockMesh(String state) {
        this.state = state;
    }

    public abstract String getModID();

    public abstract BlockShape getShape();

    public abstract BlockHitbox[] getHitbox();

    public abstract int[] getUVCoordinates(BlockFace blockFace);

    public abstract byte getLightDiffuse(LightChannels channel);

    public abstract byte getLightEmitting(LightChannels channel);

    public abstract boolean shouldRenderTransparentMesh();

    public abstract boolean shouldRenderDecorationMesh();

    public abstract boolean isSelfOccluded();

    public abstract boolean isRotatable();

    public abstract boolean canPlaceOn();

    public String getState() {
        return state;
    }
}
