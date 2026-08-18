package omnivoxel.tools;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

public class SplineRenderer {

    private int vao;
    private int vbo;

    private int maxVertices;

    public void init(int maxVertices) {
        this.maxVertices = maxVertices;

        vao = GL30C.glGenVertexArrays();
        vbo = GL15C.glGenBuffers();

        GL30C.glBindVertexArray(vao);

        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                vbo
        );

        GL15C.glBufferData(
                GL15C.GL_ARRAY_BUFFER,
                (long) maxVertices
                        * 2
                        * Float.BYTES,
                GL15C.GL_DYNAMIC_DRAW
        );

        GL20C.glEnableVertexAttribArray(0);

        GL20C.glVertexAttribPointer(
                0,
                2,
                GL11C.GL_FLOAT,
                false,
                2 * Float.BYTES,
                0
        );

        GL30C.glBindVertexArray(0);
    }

    public void render(
            SplineMapper mapper,
            int selectedPoint,
            float graphX,
            float graphY,
            float graphWidth,
            float graphHeight,
            int samples
    ) {
        GL11C.glDisable(GL11C.GL_DEPTH_TEST);
        GL11C.glDisable(GL11C.GL_CULL_FACE);

        renderGrid(
                graphX,
                graphY,
                graphWidth,
                graphHeight
        );

        List<SplineMapper.Point> curve =
                mapper.generateCurve(samples);

        renderLine(
                curve,
                graphX,
                graphY,
                graphWidth,
                graphHeight
        );

        renderPoints(
                mapper.getPoints(),
                selectedPoint,
                graphX,
                graphY,
                graphWidth,
                graphHeight
        );
    }

    private void renderGrid(
            float x,
            float y,
            float width,
            float height
    ) {
        float[] vertices = {
                x, y,
                x + width, y,

                x, y + height,
                x + width, y + height,

                x, y,
                x, y + height,

                x + width, y,
                x + width, y + height
        };

        upload(vertices);

        GL11C.glDrawArrays(
                GL11C.GL_LINES,
                0,
                8
        );
    }

    private void renderLine(
            List<SplineMapper.Point> points,
            float graphX,
            float graphY,
            float graphWidth,
            float graphHeight
    ) {
        FloatBuffer buffer =
                MemoryUtil.memAllocFloat(
                        points.size() * 2
                );

        for (SplineMapper.Point point : points) {
            buffer.put(
                    graphX
                            + (float) point.location()
                            * graphWidth
            );

            buffer.put(
                    graphY
                            + (1.0f - (float) point.value())
                            * graphHeight
            );
        }

        buffer.flip();

        GL30C.glBindVertexArray(vao);

        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                vbo
        );

        GL15C.glBufferSubData(
                GL15C.GL_ARRAY_BUFFER,
                0,
                buffer
        );

        GL11C.glDrawArrays(
                GL11C.GL_LINE_STRIP,
                0,
                points.size()
        );

        MemoryUtil.memFree(buffer);
    }

    private void renderPoints(
            List<SplineMapper.Point> points,
            int selectedPoint,
            float graphX,
            float graphY,
            float graphWidth,
            float graphHeight
    ) {
        FloatBuffer buffer =
                MemoryUtil.memAllocFloat(
                        points.size() * 2
                );

        for (SplineMapper.Point point : points) {
            buffer.put(
                    graphX
                            + (float) point.location()
                            * graphWidth
            );

            buffer.put(
                    graphY
                            + (1.0f - (float) point.value())
                            * graphHeight
            );
        }

        buffer.flip();

        GL30C.glBindVertexArray(vao);

        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                vbo
        );

        GL15C.glBufferSubData(
                GL15C.GL_ARRAY_BUFFER,
                0,
                buffer
        );

        GL11C.glPointSize(10.0f);

        GL11C.glDrawArrays(
                GL11C.GL_POINTS,
                0,
                points.size()
        );

        MemoryUtil.memFree(buffer);
    }

    private void upload(float[] vertices) {
        FloatBuffer buffer =
                MemoryUtil.memAllocFloat(
                        vertices.length
                );

        buffer.put(vertices).flip();

        GL30C.glBindVertexArray(vao);

        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                vbo
        );

        GL15C.glBufferSubData(
                GL15C.GL_ARRAY_BUFFER,
                0,
                buffer
        );

        MemoryUtil.memFree(buffer);
    }

    public void cleanup() {
        if (vao != 0) {
            GL30C.glDeleteVertexArrays(vao);
        }

        if (vbo != 0) {
            GL15C.glDeleteBuffers(vbo);
        }

        vao = 0;
        vbo = 0;
    }
}