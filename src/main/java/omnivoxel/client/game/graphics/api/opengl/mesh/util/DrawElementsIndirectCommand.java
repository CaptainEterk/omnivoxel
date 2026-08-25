package omnivoxel.client.game.graphics.api.opengl.mesh.util;

public record DrawElementsIndirectCommand(
        int count,
        int instanceCount,
        int firstIndex,
        int baseVertex,
        int baseInstance
) {}