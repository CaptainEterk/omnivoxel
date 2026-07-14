package omnivoxel.client.game.graphics.api.opengl.image;

import omnivoxel.common.settings.ConstantClientSettings;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageLoader {
    public static Image load(String path) {
        int width, height;
        ByteBuffer imageBuffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            imageBuffer = STBImage.stbi_load(ConstantClientSettings.DATA_LOCATION + path, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (imageBuffer == null) {
                throw new RuntimeException(
                        "Failed to load texture file " + ConstantClientSettings.DATA_LOCATION + path + "\n" + STBImage.stbi_failure_reason());
            }

            width = widthBuffer.get();
            height = heightBuffer.get();
        }

//        int width = 256;
//        int height = 256;
//
//        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(width * height * 4);
//
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                imageBuffer.put((byte) Math.floor(Math.random()*255)); // R
//                imageBuffer.put((byte) 0);   // G
//                imageBuffer.put((byte) 0);   // B
//                imageBuffer.put((byte) 255); // A
//            }
//        }
//
//        imageBuffer.flip();
//        Need to not free imageBuffer in TextureLoader if using procedural textures

        return new Image(imageBuffer, width, height);
    }
}