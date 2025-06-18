package core.blocks;

import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.shape.BlockShape;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.util.cache.IDCache;

import java.util.Objects;

// TODO: Make this a mod
public class GlassBlock extends Block {
    private final Shape shape;
    private final int[] uvCoords;

    public GlassBlock(IDCache<Shape> shapeCache) {
        this.shape = shapeCache.get("omnivoxel:block_shape", BlockShape.class);
        this.uvCoords = new int[]{
                1, 1,
                2, 1,
                2, 2,
                1, 2
        };
    }

    @Override
    public String getID() {
        return "glass_block";
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
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
        return !Objects.equals(adjacentBlock.getModID(), getModID()) && super.shouldRenderFace(face, adjacentBlock);
    }
}