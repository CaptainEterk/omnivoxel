package omnivoxel.client.game.graphics.opengl.text;

import omnivoxel.client.game.graphics.opengl.text.font.Font;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.stb.STBTTBakedChar;

import java.util.ArrayList;
import java.util.List;

public class TextRenderer {
    private static final float TAB_SIZE = 40;
    private int vaoID;
    private int vboID;

    public void init() {
        // Create and bind a VAO
        vaoID = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vaoID);

        // Create a VBO for the vertices
        vboID = GL15C.glGenBuffers();
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboID);

        // Define the vertex positions and texture coordinates for the characters (initially empty)
        GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, 0, GL15C.GL_DYNAMIC_DRAW);

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

        // Prepare to accumulate the vertices and texture coordinates for all characters
        List<Float> verticesList = new ArrayList<>();

        String[] lines = text.split("[\r\n]");
        for (int i = 0; i < lines.length; i++) {
            // Render a line of text and accumulate the vertex data
            accumulateLine(font, lines[i], x, y + 32 * (i + 2) * scale, scale, alignment, verticesList);
        }

        // Convert accumulated vertices to array
        float[] verticesArray = new float[verticesList.size()];
        for (int i = 0; i < verticesArray.length; i++) {
            verticesArray[i] = verticesList.get(i);
        }

        // Update VBO with the accumulated vertices data
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vboID);
        GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, verticesArray, GL15C.GL_DYNAMIC_DRAW);
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);

        // Render the accumulated text
        GL30C.glBindVertexArray(vaoID);
        GL11.glDrawArrays(GL30C.GL_TRIANGLES, 0, verticesArray.length / 4);
        GL30C.glBindVertexArray(0);
    }

    private void accumulateLine(Font font, String text, float x, float y, float scale, Alignment alignment, List<Float> verticesList) {
        float startX = x;

        if (alignment != Alignment.LEFT) {
            // Calculate total width of the text
            float totalWidth = 0;
            for (char c : text.toCharArray()) {
                if (c < 32 || c > 126) continue;
                STBTTBakedChar charInfo = font.charData().get(c - 32);
                totalWidth += getCharWidth(charInfo, scale);
            }

            // Adjust startX based on alignment
            if (alignment == Alignment.CENTER) {
                startX -= totalWidth / 2;
            } else if (alignment == Alignment.RIGHT) {
                startX -= totalWidth;
            }
        }

        // Now accumulate the vertices for each character
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                startX += TAB_SIZE * scale;
            }
            if (c < 32 || c > 126) {
                continue; // Skip non-printable characters
            }

            STBTTBakedChar charInfo = font.charData().get(c - 32);

            // Calculate position and size
            float xPos = startX + getCharOffset(charInfo, scale);
            // Correct the vertical position for each character
            float yPos = y - 32 * scale + charInfo.yoff() * scale;

            // Apply the correct scale for height and width based on font metrics
            float w = (charInfo.x1() - charInfo.x0()) * scale;
            float h = (charInfo.y1() - charInfo.y0()) * scale;

            // Add the character quad to the vertex list
            addQuadVertices(verticesList, xPos, yPos, w, h, font, charInfo);

            // Move startX to the right for the next character
            startX += getCharWidth(charInfo, scale);
        }
    }

    private float getCharOffset(STBTTBakedChar charInfo, float scale) {
        return charInfo.xoff() * scale;
    }

    private float getCharWidth(STBTTBakedChar charInfo, float scale) {
        return charInfo.xadvance() * scale;
    }

    private void addQuadVertices(List<Float> verticesList, float x, float y, float w, float h, Font font, STBTTBakedChar charInfo) {
        // Adjust texture coordinates to account for OpenGL's origin being at the bottom-left
        float sMin = (float) charInfo.x0() / font.bitmapWidth();
        float tMax = (float) charInfo.y0() / font.bitmapHeight(); // Bottom of the texture
        float sMax = (float) charInfo.x1() / font.bitmapWidth();
        float tMin = (float) charInfo.y1() / font.bitmapHeight(); // Top of the texture

        // Add vertices for the quad
        verticesList.add(x);
        verticesList.add(y);
        verticesList.add(sMin);
        verticesList.add(tMax);  // Top-left

        verticesList.add(x + w);
        verticesList.add(y);
        verticesList.add(sMax);
        verticesList.add(tMax);  // Top-right

        verticesList.add(x + w);
        verticesList.add(y + h);
        verticesList.add(sMax);
        verticesList.add(tMin);  // Bottom-right

        verticesList.add(x);
        verticesList.add(y);
        verticesList.add(sMin);
        verticesList.add(tMax);  // Top-left

        verticesList.add(x + w);
        verticesList.add(y + h);
        verticesList.add(sMax);
        verticesList.add(tMin);  // Bottom-right

        verticesList.add(x);
        verticesList.add(y + h);
        verticesList.add(sMin);
        verticesList.add(tMin);  // Bottom-left
    }

    public void cleanup() {
        // Cleanup VBO and VAO
        GL30C.glDeleteBuffers(vboID);
        GL30C.glDeleteVertexArrays(vaoID);
    }
}