package omnivoxel.server.entity;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.io.entity.EntityIO;
import omnivoxel.util.math.Position3D;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityStorage {
    private final Map<Position3D, ChunkEntityStorage> chunks;
    private int request = 0;

    public EntityStorage() {
        chunks = new ConcurrentHashMap<>();
    }

    public void tick() {
        for (Map.Entry<Position3D, ChunkEntityStorage> entry : chunks.entrySet()) {
            checkOldEntityStorageChunk(entry.getKey(), entry.getValue());
        }
    }

    private void checkOldEntityStorageChunk(Position3D position3D, ChunkEntityStorage chunkEntityStorage) {
        if (chunkEntityStorage.shouldSave(this.request)) {
            try {
                EntityIO.writeEntities(position3D, chunkEntityStorage.entities);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Set<Entity> getEntitiesInChunk(Position3D chunkPosition) {
        ChunkEntityStorage chunkValue = chunks.get(chunkPosition);
        return chunkValue == null ? null : chunkValue.get(request);
    }

    private static final class ChunkEntityStorage {
        private final Set<Entity> entities;
        private int request;

        private ChunkEntityStorage(Set<Entity> entities, int request) {
            this.entities = entities;
            this.request = request;
        }

        public boolean shouldSave(int request) {
            return request - this.request > ConstantCommonSettings.CHUNK_TICK_TIMEOUT;
        }

        public Set<Entity> get(int request) {
            this.request = request;
            return entities;
        }
    }
}