package omnivoxel.server;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.math.Position3D;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorld {
    private final ServerBlock air;
    private final Map<Position3D, Chunk> chunks;

    public ServerWorld(ServerBlock air) {
        this.air = air;
        chunks = new ConcurrentHashMap<>();
    }

    public void add(Position3D position3D, Chunk chunk) {
        this.chunks.put(position3D, chunk);
    }

    public Chunk get(Position3D position3D) {
        return chunks.get(position3D);
    }

    public @NotNull ServerBlock getBlock(Position3D chunkPosition, int x, int y, int z) {
        final int CW = ConstantGameSettings.CHUNK_WIDTH;
        final int CH = ConstantGameSettings.CHUNK_HEIGHT;
        final int CL = ConstantGameSettings.CHUNK_LENGTH;

        int dx = Math.floorDiv(x, CW);
        int dy = Math.floorDiv(y, CH);
        int dz = Math.floorDiv(z, CL);

        Position3D neighborChunk = chunkPosition.add(dx, dy, dz);
        Chunk chunk = get(neighborChunk);
        if (chunk == null) return air;

        int lx = Math.floorMod(x, CW);
        int ly = Math.floorMod(y, CH);
        int lz = Math.floorMod(z, CL);

        return chunk.getBlock(lx, ly, lz);
    }
}