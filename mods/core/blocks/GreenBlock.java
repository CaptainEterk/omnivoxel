package core.blocks;

import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.BlockShape;
import omnivoxel.client.game.thread.mesh.shape.Shape;

import java.util.Objects;

// TODO: Make this a mod
public class GreenBlock extends Block {
    private final Shape shape;
    private final int[] uvCoords;

    public GreenBlock(BlockShape shape) {
        this.shape = shape;
        this.uvCoords = new int[]{
                15, 0,
                16, 0,
                16, 1,
                15, 1
        };
    }

    @Override
    public String getID() {
        return "green_block";
    }

    @Override
    public String getModID() {
        return "core:" + getID();
    }

    @Override
    public Shape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
        return shape;
    }

    @Override
    public int[] getUVCoordinates(BlockFace blockFace) {
        return uvCoords;
    }

    @Override
    public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
        return !Objects.equals(adjacentBlock.getModID(), getModID());
    }
}