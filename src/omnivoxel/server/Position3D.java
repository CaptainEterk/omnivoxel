package omnivoxel.server;

import omnivoxel.client.game.position.ChunkPosition;

public record Position3D(int x, int y, int z) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position3D that = (Position3D) o;
        return x() == that.x() && y() == that.y() && z() == that.z();
    }

    public Position3D add(int x, int y, int z) {
        return new Position3D(this.x + x, this.y + y, this.z + z);
    }

    public ChunkPosition chunkPosition() {
        return new ChunkPosition(x, y, z);
    }
}