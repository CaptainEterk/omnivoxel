package omnivoxel.client.game.thread.mesh.meshData;

import java.nio.ByteBuffer;

public interface MeshData {
    ByteBuffer solidVertices();

    ByteBuffer solidIndices();

    ByteBuffer transparentVertices();

    ByteBuffer transparentIndices();

    void cleanup();
}