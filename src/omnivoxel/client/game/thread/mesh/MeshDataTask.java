package omnivoxel.client.game.thread.mesh;

import io.netty.buffer.ByteBuf;
import omnivoxel.math.Position3D;

public record MeshDataTask(ByteBuf blocks, Position3D position3D) {
}