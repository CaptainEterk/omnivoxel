package omnivoxel.client.game.graphics.opengl.mesh.block;

import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.common.BlockShape;

public final class AirBlock extends Block {
    @Override
    public String getID() {
        return "air";
    }

    @Override
    public String getModID() {
        return "omnivoxel:" + getID();
    }

    @Override
    public BlockShape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
        return BlockShape.EMPTY_BLOCK_SHAPE;
    }

    @Override
    public int[] getUVCoordinates(BlockFace blockFace) {
        return new int[0];
    }

    @Override
    public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }
}