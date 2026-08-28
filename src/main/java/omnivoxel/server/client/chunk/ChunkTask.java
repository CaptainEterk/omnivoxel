package omnivoxel.server.client.chunk;

import omnivoxel.server.client.ServerClient;
import omnivoxel.util.thread.WorkerTask;

import java.util.Objects;

public record ChunkTask(ServerClient serverClient, int x, int y, int z, int lod) implements WorkerTask {
    @Override
    public void reject() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChunkTask chunkTask = (ChunkTask) o;
        return x == chunkTask.x && y == chunkTask.y && z == chunkTask.z && lod == chunkTask.lod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverClient, x, y, z, lod);
    }
}