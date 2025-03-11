package omnivoxel.client.game.thread.mesh.vertex;

import omnivoxel.client.game.thread.mesh.block.face.BlockFace;

public record UniqueVertex(Vertex vertex, TextureVertex textureVertex, BlockFace blockFace) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UniqueVertex(Vertex vertex1, TextureVertex position, BlockFace face))) {
            return false;
        }
        // Vertex
        if (vertex.px() != vertex1.px() ||
                vertex.py() != vertex1.py() ||
                vertex.pz() != vertex1.pz()
        ) {
            return false;
        }
        // TexturePosition
        if (
                textureVertex.tx() != position.tx() ||
                        textureVertex.ty() != position.ty()
        ) {
            return false;
        }
        // BlockFace
        return blockFace == face;
    }
}