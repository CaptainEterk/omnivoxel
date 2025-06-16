package omnivoxel.client.game.thread.mesh.block;

import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.Shape;

public class BlockStateWrapper extends Block {
    private final Block wrappedBlock;

    public BlockStateWrapper(Block wrappedBlock, int[] state) {
        super(state);
        wrappedBlock.state = state;
        this.wrappedBlock = wrappedBlock;
    }

    @Override
    public String getID() {
        return wrappedBlock.getID();
    }

    @Override
    public String getModID() {
        return wrappedBlock.getModID();
    }

    @Override
    public Shape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
        return wrappedBlock.getShape(top, bottom, north, south, east, west);
    }

    @Override
    public int[] getUVCoordinates(BlockFace blockFace) {
        return wrappedBlock.getUVCoordinates(blockFace);
    }

    @Override
    public int[] getState() {
        return super.getState();
    }
}
