package omnivoxel.client.game.thread.mesh;

import omnivoxel.client.game.thread.mesh.block.Block;

public record UnpackedChunk(Block[] blocks, boolean allSolid) {
}