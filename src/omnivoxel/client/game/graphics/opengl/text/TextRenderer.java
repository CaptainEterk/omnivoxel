package omnivoxel.client.game.graphics.opengl.text;

import omnivoxel.client.game.graphics.opengl.text.font.Font;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class TextRenderer {
    private static final float TAB_SIZE = 40f;

    private int vaoID;
    private int vboID;

    // Tunable: maximum characters per frame
    private static final int MAX_CHARS = 2048;
    private static final int VERTICES_PER_QUAD = 6;
    private static final int FLOATS_PER_VERTEX = 4; // x, y, s, t

    private final FloatBuffer vertexBuffer =
            MemoryUtil.memAllocFloat(MAX_CHARS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX);

    private int drawCount = 0;   // number of vertices in current batch
    private Font currentFont;    // font used for batching

    public void init() {
        vaoID = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vaoID);

        vboID = GL15C.glGenBuffers();
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboID);

        // Allocate GPU memory once
        GL15C.glBufferData(
                GL15C.GL_ARRAY_BUFFER,
                (long) MAX_CHARS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX * Float.BYTES,
                GL15C.GL_DYNAMIC_DRAW
        );

        // Position attribute
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // UV attribute
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30C.glBindVertexArray(0);
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Queue text to be rendered this frame.
     * Must call flush() after queuing all text.
     */
    public void queueText(Font font, String text, float x, float y, float scale, Alignment alignment) {
        if (currentFont == null) {
            currentFont = font;
        } else if (currentFont != font) {
            flush();
            currentFont = font;
        }

        String[] lines = text.split("[\r\n]");
        for (int i = 0; i < lines.length; i++) {
            accumulateLine(font, lines[i], x, y + 32 * (i + 2) * scale, scale, alignment);
        }
    }

    private void accumulateLine(Font font, String text, float x, float y, float scale, Alignment alignment) {
        float startX = x;

        if (alignment != Alignment.LEFT) {
            float totalWidth = 0;
            for (char c : text.toCharArray()) {
                if (c >= 32 && c <= 126) {
                    STBTTBakedChar charInfo = font.charData().get(c - 32);
                    totalWidth += getCharWidth(charInfo, scale);
                }
            }
            if (alignment == Alignment.CENTER) {
                startX -= totalWidth / 2f;
            } else if (alignment == Alignment.RIGHT) {
                startX -= totalWidth;
            }
        }

        for (char c : text.toCharArray()) {
            if (c == '\t') {
                startX += TAB_SIZE * scale;
                continue;
            }
            if (c < 32 || c > 126) continue;

            STBTTBakedChar charInfo = font.charData().get(c - 32);

            float xPos = startX + charInfo.xoff() * scale;
            float yPos = y - 32 * scale + charInfo.yoff() * scale;
            float w = (charInfo.x1() - charInfo.x0()) * scale;
            float h = (charInfo.y1() - charInfo.y0()) * scale;

            addQuadVertices(xPos, yPos, w, h, font, charInfo);
            startX += getCharWidth(charInfo, scale);
        }
    }

    private float getCharWidth(STBTTBakedChar charInfo, float scale) {
        return charInfo.xadvance() * scale;
    }

    private void addQuadVertices(float x, float y, float w, float h, Font font, STBTTBakedChar charInfo) {
        float sMin = (float) charInfo.x0() / font.bitmapWidth();
        float tMax = (float) charInfo.y0() / font.bitmapHeight();
        float sMax = (float) charInfo.x1() / font.bitmapWidth();
        float tMin = (float) charInfo.y1() / font.bitmapHeight();

        putVertex(x, y, sMin, tMax);
        putVertex(x + w, y, sMax, tMax);
        putVertex(x + w, y + h, sMax, tMin);

        putVertex(x, y, sMin, tMax);
        putVertex(x + w, y + h, sMax, tMin);
        putVertex(x, y + h, sMin, tMin);
    }

    private void putVertex(float x, float y, float s, float t) {
        if (drawCount >= MAX_CHARS * VERTICES_PER_QUAD) return; // prevent overflow
        vertexBuffer.put(x).put(y).put(s).put(t);
        drawCount++;
    }

    /**
     * Upload all queued text to GPU and render in one draw call.
     */
    public void flush() {
        if (drawCount == 0 || currentFont == null) return;

        vertexBuffer.flip();

        GL30C.glActiveTexture(GL13C.GL_TEXTURE0);
        GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, currentFont.textureID());

        GL30C.glBindVertexArray(vaoID);
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboID);

        GL15C.glBufferSubData(GL15C.GL_ARRAY_BUFFER, 0, vertexBuffer);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, drawCount);

        GL30C.glBindVertexArray(0);

        vertexBuffer.clear();
        drawCount = 0;
        currentFont = null;
    }

    public void cleanup() {
        GL30C.glDeleteBuffers(vboID);
        GL30C.glDeleteVertexArrays(vaoID);
        MemoryUtil.memFree(vertexBuffer);
    }
}