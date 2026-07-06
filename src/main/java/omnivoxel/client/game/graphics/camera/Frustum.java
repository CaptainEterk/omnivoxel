package omnivoxel.client.game.graphics.camera;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;
import omnivoxel.util.math.Position3D;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

public class Frustum {
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();

    public void updateFrustum(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        Matrix4f viewProjMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);
        frustumIntersection.set(viewProjMatrix);
    }

    public boolean isChunkInFrustum(Position3D position3D) {
        int x = IndexCalculator.blockX(position3D.x());
        int y = IndexCalculator.blockY(position3D.y());
        int z = IndexCalculator.blockZ(position3D.z());

        return frustumIntersection.testAab(
                x, y, z,
                x + ConstantCommonSettings.CHUNK_WIDTH,
                y + ConstantCommonSettings.CHUNK_HEIGHT,
                z + ConstantCommonSettings.CHUNK_LENGTH
        );
    }
}