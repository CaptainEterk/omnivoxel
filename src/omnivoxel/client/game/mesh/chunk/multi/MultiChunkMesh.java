package omnivoxel.client.game.mesh.chunk.multi;

import omnivoxel.client.game.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.thread.mesh.meshData.GeneralMeshData;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class MultiChunkMesh {
    private final ChunkMesh[] chunkMeshes;
    private final int size;
    private ChunkMesh combined;

    public MultiChunkMesh(int size) {
        chunkMeshes = new ChunkMesh[size * size * size];
        this.size = size;
    }

    public void combine() {
        ByteBuffer solidVertices = concat(
                Arrays.stream(chunkMeshes)
                        .map(ChunkMesh::meshData)
                        .map(MeshData::solidVertices)
                        .toArray(ByteBuffer[]::new)
        );
        ByteBuffer solidIndices = concat(
                Arrays.stream(chunkMeshes)
                        .map(ChunkMesh::meshData)
                        .map(MeshData::solidIndices)
                        .toArray(ByteBuffer[]::new)
        );
        ByteBuffer transparentVertices = concat(
                Arrays.stream(chunkMeshes)
                        .map(ChunkMesh::meshData)
                        .map(MeshData::transparentVertices)
                        .toArray(ByteBuffer[]::new)
        );
        ByteBuffer transparentIndices = concat(
                Arrays.stream(chunkMeshes)
                        .map(ChunkMesh::meshData)
                        .map(MeshData::transparentIndices)
                        .toArray(ByteBuffer[]::new)
        );
        MeshData meshData = new GeneralMeshData(solidVertices, solidIndices, transparentVertices, transparentIndices);
        // Free all the vaos, and vbos, remove them from the world, create a new vao/vbo
    }

    private ByteBuffer concat(ByteBuffer[] byteBuffers) {
        int totalCapacity = 0;
        for (ByteBuffer buffer : byteBuffers) {
            totalCapacity += buffer.remaining();
        }

        ByteBuffer combined = ByteBuffer.allocate(totalCapacity);

        for (ByteBuffer buffer : byteBuffers) {
            ByteBuffer duplicate = buffer.duplicate();
            duplicate.flip();
            combined.put(duplicate);
        }

        combined.flip();
        return combined;
    }

    public void setMesh(int x, int y, int z, ChunkMesh chunkMesh) {
        chunkMeshes[calculateIndex(x, y, z)] = chunkMesh;
    }

    private int calculateIndex(int x, int y, int z) {
        return x + (y * this.size) + (z * size * size);
    }
}