package omnivoxel.client.game.thread.mesh;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.thread.mesh.block.Block;

public record MeshDataTask(Block[] blocks, ChunkPosition chunkPosition) {
}