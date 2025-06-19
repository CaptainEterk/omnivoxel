package core.blocks;

import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.shape.BlockShape;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.util.cache.IDCache;

// TODO: Make this a mod
public class LogBlock extends Block {
    private final Shape shape;
    private final int[] sideUVCoords;
    private final int[] topUVCoords;

    public LogBlock(IDCache<String, Shape> shapeCache) {
        this.shape = shapeCache.get("omnivoxel:block_shape", BlockShape.class);
        this.sideUVCoords = new int[]{
                2, 1,
                3, 1,
                3, 2,
                2, 2
        };
        this.topUVCoords = new int[]{
                1, 2,
                2, 2,
                2, 3,
                1, 3
        };
    }

    @Override
    public String getID() {
        return "log_block";
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
        return blockFace == BlockFace.TOP || blockFace == BlockFace.BOTTOM ? topUVCoords : sideUVCoords;
    }
}