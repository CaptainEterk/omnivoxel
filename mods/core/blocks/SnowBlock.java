package core.blocks;

import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.BlockShape;
import omnivoxel.client.game.thread.mesh.shape.Shape;

// TODO: Make this a mod
public class SnowBlock extends Block {
    private final Shape shape;
    private final int[] uvCoords;

    public SnowBlock(BlockShape shape) {
        this.shape = shape;
        this.uvCoords = new int[]{
                4, 2,
                4, 3,
                3, 3,
                3, 2
        };
    }

    @Override
    public String getID() {
        return "snow_block";
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