package omnivoxel.client.game.graphics.opengl.mesh.generators;

import omnivoxel.world.block.Block;
import omnivoxel.world.chunk.Chunk;

public record ChunkBlockData(Chunk<Block> chunk, omnivoxel.client.game.graphics.opengl.mesh.block.Block[] blocks) {
}