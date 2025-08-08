package omnivoxel.client.game.graphics.opengl.mesh;

import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;

public record ChunkFace(int x, int y, int z, BlockFace blockFace) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChunkFace chunkFace = (ChunkFace) o;
        return x == chunkFace.x && y == chunkFace.y && z == chunkFace.z && blockFace == chunkFace.blockFace;
    }
}