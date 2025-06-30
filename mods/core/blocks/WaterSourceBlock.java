package core.blocks;

import omnivoxel.client.game.graphics.opengl.mesh.block.AirBlock;
import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.shape.ShallowBlockShape;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.client.game.graphics.opengl.shape.TransparentBlockShape;
import omnivoxel.util.cache.IDCache;

import java.util.Objects;

// TODO: Make this a mod
public class WaterSourceBlock extends Block {
    private final Shape shallowBlockShape;
    private final Shape blockShape;
    private final int[] uvCoords;

    public WaterSourceBlock(IDCache<String, Shape> shapeCache) {
        this.blockShape = shapeCache.get("omnivoxel:transparent_block_shape", TransparentBlockShape.class);
        this.shallowBlockShape = shapeCache.get("omnivoxel:shallow_block_shape_2", ShallowBlockShape.class, new Class[]{Integer.class}, new Object[]{2});
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