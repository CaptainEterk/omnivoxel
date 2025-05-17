package omnivoxel.world;

import omnivoxel.math.Position3D;
import omnivoxel.server.client.block.ServerBlock;

public interface DynamicWorld<T> extends World<T> {
    void setBlock(Position3D position3D, ServerBlock block);
}