package omnivoxel.client.game.graphics.api.opengl.mesh;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Quad {

    private int vao;
    private int vbo;
    private int ebo;

    private float x;
    private float y;
    private float width;
    private float height;

    public Quad(float width, float height) {
        this(0.0f, 0.0f, width, height);
    }

    public Quad(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;

        if (vao != 0) {
            updateVertices();
        }
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;

        if (vao != 0) {
            updateVertices();
        }
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (vao != 0) {
            updateVertices();
        }
    }

    public void init() {
        vao = GL30C.glGenVertexArrays();
        vbo = GL30C.glGenBuffers();
        ebo = GL30C.glGenBuffers();

        GL30C.glBindVertexArray(vao);

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(16);

        vertexBuffer.put(vertices());
        vertexBuffer.flip();

        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vbo);
        GL30C.glBufferData(
                GL30C.GL_ARRAY_BUFFER,
                vertexBuffer,
                GL30C.GL_DYNAMIC_DRAW
        );

        MemoryUtil.memFree(vertexBuffer);

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        GL30C.glBindBuffer(
                GL30C.GL_ELEMENT_ARRAY_BUFFER,
                ebo
        );

        GL30C.glBufferData(
                GL30C.GL_ELEMENT_ARRAY_BUFFER,
                indexBuffer,
                GL30C.GL_STATIC_DRAW
        );

        MemoryUtil.memFree(indexBuffer);

        int stride = 4 * Float.BYTES;

        GL30C.glEnableVertexAttribArray(5);
        GL30C.glVertexAttribPointer(
                5,
                2,
                GL30C.GL_FLOAT,
                false,
                stride,
                0
        );

        GL30C.glEnableVertexAttribArray(6);
        GL30C.glVertexAttribPointer(
                6,
                2,
                GL30C.GL_FLOAT,
                false,
                stride,
                2L * Float.BYTES
        );

        GL30C.glBindVertexArray(0);
    }

    private float[] vertices() {
        return new float[] {
                x,          y,           0.0f, 0.0f,
                x + width,  y,           1.0f, 0.0f,
                x + width,  y + height,  1.0f, 1.0f,
                x,          y + height,  0.0f, 1.0f
        };
    }

    private void updateVertices() {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16);

        buffer.put(vertices());
        buffer.flip();

        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vbo);

        GL30C.glBufferSubData(
                GL30C.GL_ARRAY_BUFFER,
                0,
                buffer
        );

        MemoryUtil.memFree(buffer);
    }

    public void render() {
        GL30C.glBindVertexArray(vao);

        GL30C.glDrawElements(
                GL30C.GL_TRIANGLES,
                6,
                GL30C.GL_UNSIGNED_INT,
                0
        );

        GL30C.glBindVertexArray(0);
    }

    public void cleanup() {
        if (vao != 0) {
            GL30C.glDeleteVertexArrays(vao);
        }

        if (vbo != 0) {
            GL30C.glDeleteBuffers(vbo);
        }

        if (ebo != 0) {
            GL30C.glDeleteBuffers(ebo);
        }

        vao = 0;
        vbo = 0;
        ebo = 0;
    }
}