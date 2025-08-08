package omnivoxel.client.game.graphics.opengl.shape;

import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;

public interface Shape {
    Vertex[] getVerticesOnFace(BlockFace blockFace);

    int[] getIndicesOnFace(BlockFace blockFace);

    boolean isFaceSolid(BlockFace blockFace);
}