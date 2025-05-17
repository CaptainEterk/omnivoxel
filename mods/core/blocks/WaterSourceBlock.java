package core.blocks;

import omnivoxel.client.game.thread.mesh.block.AirBlock;
import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.shape.ShallowBlockShape;
import omnivoxel.client.game.thread.mesh.shape.Shape;

import java.util.Objects;

// TODO: Make this a mod
public class WaterSourceBlock extends Block {
    private final Shape shallowBlockShape;
    private final Shape blockShape;
    private final int[] uvCoords;

    public WaterSourceBlock(ShallowBlockShape shallowBlockShape, Shape blockShape) {
        this.shallowBlockShape = shallowBlockShape;
        this.blockShape = blockShape;
        this.uvCoords = new int[]{
                3, 2,
                3, 3,
                2, 3,
                2, 2,
        };
    }

    @Override
    public String getID() {
        return "water_source_block";
    }

    @Override
    public String getModID() {
        return "core:" + getID();
    }

    @Override
    public Shape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
        return top instanceof AirBlock ? shallowBlockShape : blockShape;
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