package omnivoxel.client.game.thread.mesh.util;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.math.Position3D;

public final class PriorityUtils {
    private static Camera camera;

    public static double getPriority(Position3D position) {
        // The closer a chunk is the higher the priority
        int distanceX = position.x() * ConstantGameSettings.CHUNK_WIDTH - Math.round(camera.getX());
        int distanceY = position.y() * ConstantGameSettings.CHUNK_HEIGHT - Math.round(camera.getY());
        int distanceZ = position.z() * ConstantGameSettings.CHUNK_LENGTH - Math.round(camera.getZ());
        return (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ) / 1000.0;
    }

    public static void setCamera(Camera camera) {
        PriorityUtils.camera = camera;
    }
}