package omnivoxel.server.io;

import omnivoxel.common.settings.ConstantServerSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.io.chunk.Chunk2DCacheItem;
import omnivoxel.server.io.chunk.Chunk3DCacheItem;
import omnivoxel.server.io.chunk.ChunkIO;
import omnivoxel.server.io.entity.EntityChunkCacheItem;
import omnivoxel.server.io.entity.EntityIO;
import omnivoxel.util.math.Position2D;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk2d.Chunk2D;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class CacheHandler {
    public static void cache(CacheItem item) {
        Path path;
        byte[] bytes;
        switch (item) {
            case Chunk3DCacheItem(Position3D chunkPosition, Chunk<ServerBlock> chunk) -> {
                path = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION + chunkPosition.getPath());
                bytes = ChunkIO.encode(chunk);
            }
            case Chunk2DCacheItem(Position2D chunkPosition, Chunk2D<Integer> chunk) -> {
                path = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION, chunkPosition.getPath());
                bytes = ChunkIO.encodeIntegerChunk2D(chunk);
            }
            case EntityChunkCacheItem(Position3D chunkPosition, Set<Long> entities) -> {
                path = Path.of(ConstantServerSettings.ENTITY_SAVE_LOCATION + chunkPosition.getPath());
                bytes = EntityIO.encode(entities);
            }
            case null, default -> throw new UnsupportedOperationException(item + "is not supported yet.");
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