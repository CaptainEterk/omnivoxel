package core.blocks;

import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.shape.BlockShape;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.util.cache.IDCache;

// TODO: Make this a mod
public class GrassBlock extends Block {
    private final Shape shape;
    private final int[] topUVCoords;
    private final int[] bottomUVCoords;
    private final int[] southEastUVCoords;
    private final int[] northWestUVCoords;

    public GrassBlock(IDCache<Shape> shapeCache) {
        this.shape = shapeCache.get("omnivoxel:block_shape", BlockShape.class);
        this.topUVCoords = new int[]{
                0, 1,
                1, 1,
                1, 2,
                0, 2
        };
        this.bottomUVCoords = new int[]{
                1, 0,
                2, 0,
                2, 1,
                1, 1
        };
        this.southEastUVCoords = new int[]{
                0, 0,
                1, 0,
                1, 1,
                0, 1
        };
        this.northWestUVCoords = new int[]{
                1, 1,
                0, 1,
                0, 0,
                1, 0
        };
    }

    @Override
    public String getID() {
        return "grass_block";
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
        return switch (blockFace) {
            case TOP -> topUVCoords;
            case BOTTOM -> bottomUVCoords;
            case SOUTH, EAST -> southEastUVCoords;
            case NORTH, WEST -> northWestUVCoords;
            case NONE -> null;
        };
    }
}