package omnivoxel.client.game.world;

import omnivoxel.client.game.entity.Entity;
import omnivoxel.client.game.mesh.EntityMesh;
import omnivoxel.client.game.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.mesh.util.MeshGenerator;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.server.ConstantServerSettings;
import org.lwjgl.opengl.GL30C;

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

    private final Map<String, Entity> entities;
    private final Map<String, MeshData> nonBufferizedEntities;
    private final Map<String, EntityMesh> entityMeshes;

    public World(Client client) {
        this.client = client;
        client.setChunkListener(this::loadMeshData);
        client.setEntityListener(this::loadEntity);
        queuedChunks = ConcurrentHashMap.newKeySet();
        chunks = new ConcurrentHashMap<>();
        nonBufferizedChunks = new ConcurrentHashMap<>();

        entities = new ConcurrentHashMap<>();
        entityMeshes = new ConcurrentHashMap<>();
        nonBufferizedEntities = new ConcurrentHashMap<>();
        chunkRequestsSent = new AtomicInteger();
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

    public void bufferizeChunk(MeshGenerator meshGenerator, long endTime) {
        long startTime = System.nanoTime();
        if (!nonBufferizedChunks.isEmpty()) {
            Map.Entry<ChunkPosition, MeshData> entry = nonBufferizedChunks.entrySet().iterator().next();
            nonBufferizedChunks.remove(entry.getKey());
            ChunkMesh chunkMesh = meshGenerator.bufferizeChunkMesh(entry.getValue());
            chunks.put(entry.getKey(), chunkMesh);
            queuedChunks.remove(entry.getKey());
//            if (System.nanoTime() * 2 - startTime < endTime) {
                bufferizeChunk(meshGenerator, endTime);
//            }
        }
    }

    public void loadMeshData(ChunkPosition chunkPosition, MeshData meshData) {
        nonBufferizedChunks.put(chunkPosition, meshData);
        chunkRequestsSent.decrementAndGet();
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

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public Map<String, EntityMesh> getEntityMeshes() {
        return entityMeshes;
    }

    public Entity getEntity(String id) {
        return entities.get(id);
    }
}