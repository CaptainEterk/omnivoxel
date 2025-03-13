package omnivoxel.client.game.thread.mesh.util;

import omnivoxel.client.game.player.camera.Camera;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.position.Position;
import omnivoxel.client.game.position.WorldPosition;

public final class PriorityUtils {
    private static Camera camera;

    public static double getPriority(Position position) {
        WorldPosition worldPosition;
        if (position instanceof ChunkPosition chunkPosition) {
            worldPosition = chunkPosition.toWorldPosition(0, 0, 0);
        } else {
            worldPosition = (WorldPosition) position;
        }
        // The closer a chunk is the higher the priority
        int distanceX = worldPosition.x() - Math.round(camera.getX());
        int distanceY = worldPosition.y() - Math.round(camera.getY());
        int distanceZ = worldPosition.z() - Math.round(camera.getZ());
        return (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ) / 1000.0;
    }

    public static void setCamera(Camera camera) {
        PriorityUtils.camera = camera;
    }
}