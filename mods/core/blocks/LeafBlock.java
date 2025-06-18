package core.blocks;

import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.client.game.graphics.opengl.shape.TransparentBlockShape;
import omnivoxel.util.cache.IDCache;

import java.util.Objects;

// TODO: Make this a mod
public class LeafBlock extends Block {
    private final Shape shape;
    private final int[] uvCoords;

    public LeafBlock(IDCache<Shape> shapeCache) {
        this.shape = shapeCache.get("omnivoxel:transparent_block_shape", TransparentBlockShape.class);
        this.uvCoords = new int[]{
                0, 2,
                1, 2,
                1, 3,
                0, 3
        };
    }

    @Override
    public String getID() {
        return "leaf_block";
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
        return Objects.equals(adjacentBlock.getModID(), getModID()) || super.shouldRenderFace(face, adjacentBlock);
    }
}