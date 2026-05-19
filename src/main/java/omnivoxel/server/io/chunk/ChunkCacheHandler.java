package omnivoxel.server.io.chunk;

import omnivoxel.common.settings.ConstantServerSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.util.math.Position2D;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk2d.Chunk2D;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ChunkCacheHandler {
    public static void cache(ChunkCacheItem item) {
        Path path;
        byte[] bytes;
        if (item instanceof Chunk3DCacheItem(Position3D chunkPosition, Chunk<ServerBlock> chunk)) {
            path = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION + chunkPosition.getPath());
            bytes = ChunkIO.encode(chunk);
        } else if (item instanceof Chunk2DCacheItem(Position2D chunkPosition, Chunk2D<Integer> chunk)) {
            path = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION, chunkPosition.getPath());
            bytes = ChunkIO.encodeIntegerChunk2D(chunk);
        } else {
            throw new UnsupportedOperationException(item + "is not supported yet.");
        }
        try {
            Files.createDirectories(path.getParent());

            Path tempPath = path.resolveSibling(
                    path.getFileName() + ".tmp"
            );

            Files.write(tempPath, bytes);

            try {
                Files.move(tempPath, path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, path,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}