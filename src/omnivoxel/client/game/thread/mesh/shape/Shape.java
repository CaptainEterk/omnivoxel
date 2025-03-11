package omnivoxel.client.game.thread.mesh.shape;

import omnivoxel.client.game.thread.mesh.block.face.BlockFace;
import omnivoxel.client.game.thread.mesh.vertex.Vertex;

public interface Shape {
    Vertex[] getVerticesOnFace(BlockFace blockFace);

    int[] getIndicesOnFace(BlockFace blockFace);

    boolean isFaceSolid(BlockFace blockFace);
}