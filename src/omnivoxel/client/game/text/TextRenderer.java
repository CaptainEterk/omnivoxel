package omnivoxel.client.game.text;

import omnivoxel.client.game.text.font.Font;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.stb.STBTTBakedChar;

public class TextRenderer {
    private int vaoID;
    private int vboID;

    public void init() {
        // Define vertex positions and texture coordinates for a quad
        float[] vertices = {
                // Positions  // Texture Coords
                -0.5f, 0.5f, 0.0f, 1.0f,  // Top-left
                0.5f, 0.5f, 1.0f, 1.0f,  // Top-right
                0.5f, -0.5f, 1.0f, 0.0f,  // Bottom-right
                -0.5f, 0.5f, 0.0f, 1.0f,  // Top-left
                0.5f, -0.5f, 1.0f, 0.0f,  // Bottom-right
                -0.5f, -0.5f, 0.0f, 0.0f   // Bottom-left
        };

        // Create and bind a VAO
        vaoID = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vaoID);

        // Create and bind a VBO
        vboID = GL15C.glGenBuffers();
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboID);
        GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, vertices, GL15C.GL_STATIC_DRAW);

        // Define vertex position attribute (location = 0)
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * 4, 0);
        GL20.glEnableVertexAttribArray(0);

        // Define texture coordinate attribute (location = 1)
        GL30C.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 4, 2 * 4);
        GL20.glEnableVertexAttribArray(1);

        // Unbind VAO and VBO
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
        GL30C.glBindVertexArray(0);
    }

    public void renderText(Font font, String text, float x, float y, float scale, Alignment alignment) {
        // Bind the font texture
        GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, font.textureID());

        // Render the text
        GL30C.glBindVertexArray(vaoID);

        String[] lines = text.split("[\r\n]");
        for (int i = 0; i < lines.length; i++) {
            // Render a line of text
            renderLine(font, lines[i], x, y + 32 * (i + 2) * scale, scale, alignment);
        }

        GL30C.glBindVertexArray(0);
        GL11.glDisable(GL30C.GL_BLEND);
    }

    private void renderLine(Font font, String text, float x, float y, float scale, Alignment alignment) {
        float startX = x;

        // Calculate total width of the text
        float totalWidth = 0;
        for (char c : text.toCharArray()) {
            if (c < 32 || c > 126) continue;
            STBTTBakedChar charInfo = font.charData().get(c - 32);
            totalWidth += charInfo.xadvance() * scale;
        }

        // Adjust startX based on alignment
        if (alignment == Alignment.CENTER) {
            startX -= totalWidth / 2;
        } else if (alignment == Alignment.RIGHT) {
            startX -= totalWidth;
        }

        // Now render each character
        for (char c : text.toCharArray()) {
            if (c < 32 || c > 126) {
                continue; // Skip non-printable characters
            }

            STBTTBakedChar charInfo = font.charData().get(c - 32);

            // Calculate position and size
            float xPos = startX + charInfo.xoff() * scale;
            // Correct the vertical position for each character
            float yPos = y - 32 * scale + charInfo.yoff() * scale;

            // Apply the correct scale for height and width based on font metrics
            float w = (charInfo.x1() - charInfo.x0()) * scale;
            float h = (charInfo.y1() - charInfo.y0()) * scale;

            // Draw the quad for the character
            drawQuad(xPos, yPos, w, h, font, charInfo);

            // Move startX to the right for the next character
            startX += charInfo.xadvance() * scale;
        }
    }

    private void drawQuad(float x, float y, float w, float h, Font font, STBTTBakedChar charInfo) {
        // Adjust texture coordinates to account for OpenGL's origin being at the bottom-left
        float sMin = (float) charInfo.x0() / font.bitmapWidth();
        float tMax = (float) charInfo.y0() / font.bitmapHeight(); // Bottom of the texture
        float sMax = (float) charInfo.x1() / font.bitmapWidth();
        float tMin = (float) charInfo.y1() / font.bitmapHeight(); // Top of the texture

        // Adjust vertex positions for quad
        float[] vertices = {
                x, y, sMin, tMax,  // Top-left
                x + w, y, sMax, tMax,  // Top-right
                x + w, y + h, sMax, tMin,  // Bottom-right
                x, y, sMin, tMax,  // Top-left
                x + w, y + h, sMax, tMin,  // Bottom-right
                x, y + h, sMin, tMin   // Bottom-left
        };

        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vboID);
        GL30C.glBufferSubData(GL30C.GL_ARRAY_BUFFER, 0, vertices);
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, 0);

        GL11.glDrawArrays(GL30C.GL_TRIANGLES, 0, 6);
    }

    public void cleanup() {
        // Cleanup VBO and VAO
        GL30C.glDeleteBuffers(vboID);
        GL30C.glDeleteVertexArrays(vaoID);
    }
}