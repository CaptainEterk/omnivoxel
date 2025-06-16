package omnivoxel.client.game.thread.mesh.block;

import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.Shape;

public abstract class Block {
    protected int[] state;

    protected Block(int[] state) {
        this.state = state;
    }

    protected Block() {
        this(null);
    }

    public abstract String getID();

    public abstract String getModID();

    public abstract Shape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west);

    public abstract int[] getUVCoordinates(BlockFace blockFace);

    public boolean isTransparent() {
        return false;
    }

    public boolean shouldRenderTransparentMesh() {
        return false;
    }

    public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
        return adjacentBlock.isTransparent();
    }

    public int[] getState() {
        return state;
    }
}