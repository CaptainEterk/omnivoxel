package omnivoxel.client.game.player.camera;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CameraUpdates {
    private final AtomicBoolean shouldUpdateView = new AtomicBoolean();
    private final AtomicBoolean shouldUpdateMeshes = new AtomicBoolean();

    public void updateView() {
        shouldUpdateView.set(true);
    }

    public void updateMeshes() {
        shouldUpdateMeshes.set(true);
    }

    public void updatedView() {
        shouldUpdateView.set(false);
    }

    public void updatedMeshes() {
        shouldUpdateMeshes.set(false);
    }
}