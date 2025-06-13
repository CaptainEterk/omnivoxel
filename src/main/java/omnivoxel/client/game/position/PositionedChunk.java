package omnivoxel.client.game.position;

import omnivoxel.client.game.world.ClientWorldChunk;
import omnivoxel.math.Position3D;

public record PositionedChunk(int distance, Position3D pos, ClientWorldChunk chunk) {
}