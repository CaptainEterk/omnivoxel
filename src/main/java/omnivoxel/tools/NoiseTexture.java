package omnivoxel.tools;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class NoiseTexture {

    private final int width;
    private final int height;

    private int texture;

    public NoiseTexture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void init() {
        texture = GL11C.glGenTextures();

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_MIN_FILTER,
                GL11C.GL_LINEAR
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_MAG_FILTER,
                GL11C.GL_LINEAR
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_WRAP_S,
                GL12C.GL_CLAMP_TO_EDGE
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_WRAP_T,
                GL12C.GL_CLAMP_TO_EDGE
        );

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);
    }

    public void update(
            Noise3D noise,
            SplineMapper mapper,
            double cameraX,
            double cameraY,
            double z,
            double zoom,
            double noiseScale
    ) {
        FloatBuffer buffer =
                MemoryUtil.memAllocFloat(width * height);

        double halfWidth = width * 0.5;
        double halfHeight = height * 0.5;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double worldX =
                        cameraX + (x - halfWidth) / zoom;

                double worldY =
                        cameraY + (y - halfHeight) / zoom;

                double raw = noise.generate(
                        worldX * noiseScale,
                        worldY * noiseScale,
                        z
                );

                /*
                 * Convert [-1, 1] to [0, 1].
                 */
                double normalized =
                        raw * 0.5 + 0.5;

                normalized = Math.max(
                        0.0,
                        Math.min(1.0, normalized)
                );

                double mapped =
                        mapper.map(normalized);

                buffer.put((float) mapped);
            }
        }

        buffer.flip();

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                texture
        );

        GL11C.glTexImage2D(
                GL11C.GL_TEXTURE_2D,
                0,
                GL30C.GL_R32F,
                width,
                height,
                0,
                GL11C.GL_RED,
                GL11C.GL_FLOAT,
                buffer
        );

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                0
        );

        MemoryUtil.memFree(buffer);
    }

    public void bind(int unit) {
        GL13C.glActiveTexture(
                GL13C.GL_TEXTURE0 + unit
        );

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                texture
        );
    }

    public void cleanup() {
        if (texture != 0) {
            GL11C.glDeleteTextures(texture);
            texture = 0;
        }
    }
}