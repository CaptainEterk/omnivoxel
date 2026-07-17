package omnivoxel.client.game.graphics.api.opengl.mesh.tasks;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.graphics.api.opengl.mesh.MeshDataTask;
import omnivoxel.client.game.graphics.light.channel.LightChannels;
import omnivoxel.util.math.Position3D;

public record LightingChunkMeshDataTask(ByteBuf blocks, Position3D position3D,
                                        LightChannels channel) implements MeshDataTask {
    @Override
    public void reject() {
    }
}