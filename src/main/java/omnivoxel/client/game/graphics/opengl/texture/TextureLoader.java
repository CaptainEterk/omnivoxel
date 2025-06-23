package omnivoxel.client.game.graphics.opengl.texture;

import omnivoxel.client.game.graphics.opengl.image.Image;
import omnivoxel.client.game.graphics.opengl.image.ImageLoader;
import org.lwjgl.stb.STBImage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

public class TextureLoader {
    public static int loadTexture(String path) {
        String filePath = "assets/textures/" + path;

        Image image = ImageLoader.load(filePath);

        int textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                image.width(),
                image.height(),
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                image.image()
        );

        glGenerateMipmap(GL_TEXTURE_2D);

        STBImage.stbi_image_free(image.image());

        return textureId;
    }
}