package omnivoxel.client.game.graphics;

import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.window.Window;

import java.io.IOException;

public interface RendererAPI {
    void init() throws IOException;

    boolean shouldClose();

    void beginFrame();

    void endFrame();

    void cleanup();

    void setMipmapping(boolean mipmapped);

    Window getWindow();

    void setRenderType(RenderType renderType);

    void setShaderIVec3(String id, int x, int y, int z);

    void setShaderInt(String id, int i);

    void render(RenderMesh mesh);
}