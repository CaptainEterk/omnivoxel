package core.blocks;

import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.Shape;

import java.util.Objects;

// TODO: Make this a mod
public class IceBlock extends Block {
    private final Shape blockShape;
    private final int[] uvCoords;

    public IceBlock(Shape blockShape) {
        this.blockShape = blockShape;
        this.uvCoords = new int[]{
                3, 3,
                3, 4,
                2, 4,
                2, 3,
        };
    }

    @Override
    public String getID() {
        return "ice_block";
    }

    @Override
    public String getModID() {
        return "core:" + getID();
    }

    @Override
    public Shape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
        return blockShape;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public int[] getUVCoordinates(BlockFace blockFace) {
        return uvCoords;
    }

    @Override
    public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
        return !Objects.equals(adjacentBlock.getModID(), getModID()) && adjacentBlock.isTransparent();
    }

    @Override
    public boolean shouldRenderTransparentMesh() {
        return true;
    }
}