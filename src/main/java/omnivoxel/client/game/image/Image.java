package omnivoxel.client.game.image;

import java.nio.ByteBuffer;

public record Image(ByteBuffer image, int width, int height) {
}
