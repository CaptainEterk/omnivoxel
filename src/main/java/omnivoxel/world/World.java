package omnivoxel.world;

import omnivoxel.math.Position3D;

public interface World<T> {
    void add(Position3D position3D, T t);

    T get(Position3D position3D);
}