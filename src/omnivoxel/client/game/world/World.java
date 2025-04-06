package omnivoxel.client.game.world;

import omnivoxel.client.game.entity.Entity;
import omnivoxel.client.game.mesh.EntityMesh;
import omnivoxel.client.game.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.mesh.chunk.multi.MultiChunkMesh;
import omnivoxel.client.game.mesh.util.MeshGenerator;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.server.ConstantServerSettings;
import org.lwjgl.opengl.GL30C;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class World {
    private final Client client;

    private final Set<ChunkPosition> queuedChunks;
    private final AtomicInteger chunkRequestsSent;
    private final Map<ChunkPosition, MeshData> nonBufferizedChunks;
    private final Map<ChunkPosition, ChunkMesh> chunks;
    private final Map<ChunkPosition, MultiChunkMesh> chunkMeshes;

    private final Map<String, Entity> entities;
    private final Map<String, MeshData> nonBufferizedEntities;
    private final Map<String, EntityMesh> entityMeshes;

    private final Set<ChunkPosition> newChunks;

    private final GameState gameState;

    public World(Client client, GameState gameState) {
        this.client = client;
        this.gameState = gameState;
        client.setChunkListener(this::loadChunk);
        client.setEntityListener(this::loadEntity);
        queuedChunks = ConcurrentHashMap.newKeySet();
        chunks = new ConcurrentHashMap<>();
        chunkMeshes = new ConcurrentHashMap<>();
        nonBufferizedChunks = new ConcurrentHashMap<>();

        entities = new ConcurrentHashMap<>();
        entityMeshes = new ConcurrentHashMap<>();
        nonBufferizedEntities = new ConcurrentHashMap<>();
        chunkRequestsSent = new AtomicInteger();
        newChunks = ConcurrentHashMap.newKeySet();
    }

    public ChunkMesh getChunk(ChunkPosition chunkPosition) {
        ChunkMesh chunkMesh = chunks.get(chunkPosition);
        if (chunkMesh == null) {
            if (!queuedChunks.contains(chunkPosition)) {
                if (chunkRequestsSent.get() < ConstantServerSettings.CHUNK_REQUEST_LIMIT) {
                    client.sendRequest(new ChunkRequest(chunkPosition));
                    queuedChunks.add(chunkPosition);
                    chunkRequestsSent.incrementAndGet();
                }
            }
            return null;
        } else {
            return chunkMesh;
        }
    }

    public int bufferizeChunks(MeshGenerator meshGenerator, long endTime) {
        int count = 0;
        boolean bufferizing;
        do {
            bufferizing = bufferizeChunk(meshGenerator);
            if (bufferizing) {
                count++;
            }
        }
        while (bufferizing && count < ConstantGameSettings.BUFFERIZE_CHUNKS_PER_FRAME);
        return count;
    }

    public boolean bufferizeChunk(MeshGenerator meshGenerator) {
        if (!nonBufferizedChunks.isEmpty()) {
            Map.Entry<ChunkPosition, MeshData> entry = nonBufferizedChunks.entrySet().iterator().next();
            nonBufferizedChunks.remove(entry.getKey());
            ChunkMesh chunkMesh = meshGenerator.bufferizeChunkMesh(entry.getValue());
            chunks.put(entry.getKey(), chunkMesh);
            queuedChunks.remove(entry.getKey());
            return true;
        }
        return false;
    }

    public void loadChunk(ChunkPosition chunkPosition, MeshData meshData) {
        nonBufferizedChunks.put(chunkPosition, meshData);
        chunkRequestsSent.decrementAndGet();
        newChunks.add(chunkPosition);
        gameState.setItem("shouldCheckNewChunks", true);
    }

    public void bufferizeEntity(MeshGenerator meshGenerator) {
        if (!nonBufferizedEntities.isEmpty()) {
            Map.Entry<String, MeshData> entry = nonBufferizedEntities.entrySet().iterator().next();
            nonBufferizedEntities.remove(entry.getKey());
            EntityMesh entityMesh = meshGenerator.bufferizeEntityMesh(entry.getValue());
            entityMeshes.put(entry.getKey(), entityMesh);
        }
    }

    public void setEntity(String id, float x, float y, float z, float pitch, float yaw) {
        Entity entity = entities.get(id);
        entity.setX(x);
        entity.setY(y);
        entity.setZ(z);
        entity.setPitch(pitch);
        entity.setYaw(yaw);
    }

    public void loadEntity(String id, Entity entity) {
        entities.put(id, entity);
        nonBufferizedEntities.put(id, entity.getMeshData());
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public Map<String, EntityMesh> getEntityMeshes() {
        return entityMeshes;
    }

    public Entity getEntity(String id) {
        return entities.get(id);
    }

    public void freeAll() {
        for (ChunkMesh chunkMesh : chunks.values()) {
            GL30C.glDeleteVertexArrays(chunkMesh.solidVAO());
            GL30C.glDeleteBuffers(chunkMesh.solidVBO());
            GL30C.glDeleteBuffers(chunkMesh.solidEBO());

            GL30C.glDeleteVertexArrays(chunkMesh.transparentVAO());
            GL30C.glDeleteBuffers(chunkMesh.transparentVBO());
            GL30C.glDeleteBuffers(chunkMesh.transparentEBO());
        }
        chunks.clear();

        for (EntityMesh entityMesh : entityMeshes.values()) {
            GL30C.glDeleteVertexArrays(entityMesh.solidVAO());
            GL30C.glDeleteBuffers(entityMesh.solidVBO());
            GL30C.glDeleteBuffers(entityMesh.solidEBO());

            GL30C.glDeleteVertexArrays(entityMesh.transparentVAO());
            GL30C.glDeleteBuffers(entityMesh.transparentVBO());
            GL30C.glDeleteBuffers(entityMesh.transparentEBO());
        }
        entityMeshes.clear();
    }

    public void freeAllChunksNotIn(List<ChunkPosition> chunks) {
        if (this.chunks.keySet().containsAll(chunks) && this.chunks.size() == chunks.size()) {
            return;
        }

        // Iterate over all chunks in your current chunk map
        Iterator<Map.Entry<ChunkPosition, ChunkMesh>> iterator = this.chunks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ChunkPosition, ChunkMesh> entry = iterator.next();
            ChunkPosition chunkPosition = entry.getKey();

            // Check if the chunk position is NOT in the provided list
            if (!chunks.contains(chunkPosition)) {
                // Free the chunk (this could mean removing it from a map, deallocating memory, etc.)
                ChunkMesh chunkMesh = entry.getValue();
                GL30C.glDeleteVertexArrays(chunkMesh.solidVAO());
                GL30C.glDeleteBuffers(chunkMesh.solidVBO());
                GL30C.glDeleteBuffers(chunkMesh.solidEBO());

                GL30C.glDeleteVertexArrays(chunkMesh.transparentVAO());
                GL30C.glDeleteBuffers(chunkMesh.transparentVBO());
                GL30C.glDeleteBuffers(chunkMesh.transparentEBO());

                // Optionally remove the chunk from the map
                iterator.remove();
            }
        }
    }

    public Set<ChunkPosition> getNewChunks() {
        return newChunks;
    }

    public int totalQueuedChunks() {
        return queuedChunks.size();
    }
}