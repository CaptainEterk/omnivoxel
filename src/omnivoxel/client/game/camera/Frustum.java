package omnivoxel.client.game.camera;

import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.util.math.Position3D;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

public class Frustum {
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();

    public void updateFrustum(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        Matrix4f viewProjMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);
        frustumIntersection.set(viewProjMatrix);
    }

    private boolean isChunkInFrustum(Position3D position3D) {
        int x = position3D.x() * ConstantGameSettings.CHUNK_WIDTH;
        int y = position3D.y() * ConstantGameSettings.CHUNK_HEIGHT;
        int z = position3D.z() * ConstantGameSettings.CHUNK_LENGTH;

        return frustumIntersection.testAab(
                x,
                y,
                z,
                x + ConstantGameSettings.CHUNK_WIDTH,
                y + ConstantGameSettings.CHUNK_HEIGHT,
                z + ConstantGameSettings.CHUNK_LENGTH
        );
    }

    public boolean isMeshInFrustum(Position3D position3D) {
        return isChunkInFrustum(position3D);
    }

    public boolean isEntityInFrustum(ClientEntity clientEntity, Camera camera) {
        double x = clientEntity.getX();
        double y = clientEntity.getY();
        double z = clientEntity.getZ();

        return frustumIntersection.testPoint((float) x, (float) y, (float) z);
    }
}