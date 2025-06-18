package omnivoxel.client.game.graphics.opengl.mesh.block;

import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.shape.Shape;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;

public final class AirBlock extends Block {
    private final Shape shape = new Shape() {
        private final Vertex[] vertices = new Vertex[0];
        private final int[] indices = new int[0];

        @Override
        public Vertex[] getVerticesOnFace(BlockFace blockFace) {
            return vertices;
        }

        @Override
        public int[] getIndicesOnFace(BlockFace blockFace) {
            return indices;
        }

        @Override
        public boolean isFaceSolid(BlockFace blockFace) {
            return false;
        }
    };

    @Override
    public String getID() {
        return "air";
    }

    @Override
    public String getModID() {
        return "omnivoxel:" + getID();
    }

    @Override
    public Shape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
        return shape;
    }

    @Override
    public int[] getUVCoordinates(BlockFace blockFace) {
        return new int[0];
    }

    @Override
    public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }
}