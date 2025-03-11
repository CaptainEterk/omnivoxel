package core;

import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.BlockShape;
import omnivoxel.client.game.thread.mesh.shape.Shape;

// TODO: Make this a mod
public class StoneBlock extends Block {
    private final Shape shape;
    private final int[] uvCoords;

    public StoneBlock(BlockShape shape) {
        this.shape = shape;
        this.uvCoords = new int[]{
                2, 0,
                3, 0,
                3, 1,
                2, 1
        };
    }

    @Override
    public String getID() {
        return "stone_block";
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
}