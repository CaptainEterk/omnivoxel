package omnivoxel.client.game.graphics.opengl.mesh;

import org.joml.Matrix4f;

import java.util.Objects;

public final class EntityMesh implements Mesh {
    private final int solidVAO;
    private final int solidVBO;
    private final int solidEBO;
    private final int solidIndexCount;
    private Matrix4f model = new Matrix4f();

    public EntityMesh(
            int solidVAO, int solidVBO, int solidEBO, int solidIndexCount
    ) {
        this.solidVAO = solidVAO;
        this.solidVBO = solidVBO;
        this.solidEBO = solidEBO;
        this.solidIndexCount = solidIndexCount;
    }

    public int solidVAO() {
        return solidVAO;
    }

    public int solidVBO() {
        return solidVBO;
    }

    public int solidEBO() {
        return solidEBO;
    }

    public int solidIndexCount() {
        return solidIndexCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EntityMesh) obj;
        return this.solidVAO == that.solidVAO &&
                this.solidVBO == that.solidVBO &&
                this.solidEBO == that.solidEBO &&
                this.solidIndexCount == that.solidIndexCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(solidVAO, solidVBO, solidEBO, solidIndexCount);
    }

    @Override
    public String toString() {
        return "EntityMesh[" +
                "solidVAO=" + solidVAO + ", " +
                "solidVBO=" + solidVBO + ", " +
                "solidEBO=" + solidEBO + ", " +
                "solidIndexCount=" + solidIndexCount + ']';
    }

    public Matrix4f getModel() {
        return model;
    }

    public void setModel(Matrix4f model) {
        this.model = model;
    }
}