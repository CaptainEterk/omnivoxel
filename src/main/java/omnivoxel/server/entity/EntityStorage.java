package omnivoxel.server.entity;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.io.entity.EntityIO;
import omnivoxel.util.log.Logger;
import omnivoxel.util.math.Position3D;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityStorage {
    private final Map<Position3D, ChunkEntityStorage> chunks;
    private final Map<Long, Entity> entities;
    private int request = 0;

    public EntityStorage() {
        chunks = new ConcurrentHashMap<>();
        entities = new ConcurrentHashMap<>();
    }

    public void tick() {
        for (Map.Entry<Position3D, ChunkEntityStorage> entry : chunks.entrySet()) {
            checkOldEntityStorageChunk(entry.getKey(), entry.getValue());
        }
        request++;
    }

    private void checkOldEntityStorageChunk(Position3D position3D, ChunkEntityStorage chunkEntityStorage) {
        if (chunkEntityStorage.shouldSave(this.request)) {
            chunks.remove(position3D).entities.forEach(id -> EntityIO.writeEntity(id, this.entities.remove(id)));
        }
    }

    public Entity get(long id) {
        return entities.get(id);
    }

    public void put(Position3D chunkPosition, Entity entity, long id) {
        this.entities.put(id, entity);
        ChunkEntityStorage chunkValue = chunks.get(chunkPosition);
        if (chunkValue == null) {
            Set<Long> entities = new HashSet<>();
            entities.add(id);
            chunks.put(chunkPosition, new ChunkEntityStorage(entities, this.request));
        } else {
            chunkValue.put(id, this.request);
        }
    }

    public void move(long id, Position3D oldPosition, Position3D newPosition) {
        ChunkEntityStorage chunkEntityStorage = chunks.get(oldPosition);
        if (chunkEntityStorage != null) {
            if (!chunkEntityStorage.remove(id, this.request)) {
                Logger.warn("Tried moving an entity but wasn't found in chunk " + oldPosition);
                return;
            }

            ChunkEntityStorage newChunkEntityStorage = chunks.get(newPosition);
            if (newChunkEntityStorage != null) {
                chunkEntityStorage.put(id, this.request);
            } else {
                Set<Long> entities = new HashSet<>();
                entities.add(id);
                chunks.put(newPosition, new ChunkEntityStorage(entities, this.request));
            }
        } else {
            Logger.warn("Tried moving an entity from a non-existent chunk");
        }
    }

    private static final class ChunkEntityStorage {
        private final Set<Long> entities;
        private int request;

        private ChunkEntityStorage(Set<Long> entities, int request) {
            this.entities = entities;
            this.request = request;
        }

        public boolean shouldSave(int request) {
            return request - this.request > ConstantCommonSettings.CHUNK_TICK_TIMEOUT;
        }

        public Set<Long> get(int request) {
            this.request = request;
            return entities;
        }

        public boolean remove(long id, int request) {
            this.request = request;
            return entities.remove(id);
        }

        public void put(long id, int request) {
            this.request = request;
            entities.add(id);
        }
    }
}