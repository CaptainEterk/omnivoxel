package omnivoxel.client.game.graphics.api.opengl.mesh.util;

import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class ChunkIndirectBuffer {
    /*
     * OpenGL DrawElementsIndirectCommand:
     *
     * uint count;
     * uint instanceCount;
     * uint firstIndex;
     * uint baseVertex;
     * uint baseInstance;
     *
     * 5 uints = 20 bytes
     */
    public static final int COMMAND_SIZE = 5 * Integer.BYTES;

    private int buffer;
    private int capacity;

    public void init(int maxDraws) {
        capacity = maxDraws;

        buffer = GL45C.glCreateBuffers();

        GL45C.glNamedBufferStorage(
                buffer,
                (long) maxDraws * COMMAND_SIZE,
                GL45C.GL_DYNAMIC_STORAGE_BIT
        );
    }

    public int buffer() {
        return buffer;
    }

    public int capacity() {
        return capacity;
    }

    public void clear() {
        ByteBuffer zero = org.lwjgl.system.MemoryUtil.memCalloc(
                capacity * COMMAND_SIZE
        );

        try {
            GL45C.glNamedBufferSubData(buffer, 0, zero);
        } finally {
            org.lwjgl.system.MemoryUtil.memFree(zero);
        }
    }

    public void cleanup() {
        if (buffer != 0) {
            GL45C.glDeleteBuffers(buffer);
            buffer = 0;
        }

        capacity = 0;
    }
}