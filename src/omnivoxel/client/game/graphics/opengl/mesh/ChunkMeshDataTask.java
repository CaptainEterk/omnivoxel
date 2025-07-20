package omnivoxel.client.game.graphics.opengl.mesh;

import io.netty.buffer.ByteBuf;
import omnivoxel.util.math.Position3D;

public record ChunkMeshDataTask(ByteBuf blocks, Position3D position3D) implements MeshDataTask {
    @Override
    public void cleanup() {
        blocks.release();
    }
}