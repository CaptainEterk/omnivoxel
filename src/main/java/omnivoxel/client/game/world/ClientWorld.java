package omnivoxel.client.game.world;

import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.ChunkMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.graphics.opengl.mesh.util.MeshGenerator;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.math.Position3D;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.util.cache.IDCache;
import org.lwjgl.opengl.GL30C;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ClientWorld {
    private final Set<Position3D> queuedChunks;
    private final AtomicInteger chunkRequestsSent;
    private final AtomicInteger chunkResponseGotten;
    private final Queue<MeshData> nonBufferizedChunks;
    private final Set<Position3D> newChunks;
    private final GameState gameState;
    private final Map<Position3D, ClientWorldChunk> chunks;
    private Client client;
    private boolean requesting = true;
    private final Map<String, ClientEntity> entities;
    private final IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache;
    private final Set<String> queuedEntityMeshData;

    public ClientWorld(GameState gameState) {
        this.gameState = gameState;
        queuedChunks = ConcurrentHashMap.newKeySet();
        nonBufferizedChunks = new ConcurrentLinkedDeque<>();
        this.chunks = new ConcurrentHashMap<>();
        this.entityMeshDefinitionCache = new IDCache<>();

        chunkRequestsSent = new AtomicInteger();
        chunkResponseGotten = new AtomicInteger();
        newChunks = ConcurrentHashMap.newKeySet();
        entities = new ConcurrentHashMap<>();
        queuedEntityMeshData = ConcurrentHashMap.newKeySet();
    }

    public int totalQueuedChunks() {
        return queuedChunks.size();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public int size() {
        return chunks.size();
    }

    public ClientWorldChunk get(Position3D position3D, boolean request) {
        ClientWorldChunk clientWorldChunk = chunks.get(position3D);
        if (clientWorldChunk != null) {
            return clientWorldChunk;
        } else if (request && requesting) {
            int inflightRequests = chunkRequestsSent.get() - chunkResponseGotten.get();
            if (inflightRequests < ConstantServerSettings.CHUNK_REQUEST_LIMIT && queuedChunks.add(position3D)) {
                client.sendRequest(new ChunkRequest(position3D));
                chunkRequestsSent.incrementAndGet();
            } else {
                requesting = false;
            }
        }
        return null;
    }

    public Position3D[] getKeys() {
        return chunks.keySet().toArray(new Position3D[0]);
    }

    public ClientWorldChunk[] getValues() {
        return chunks.values().toArray(new ClientWorldChunk[0]);
    }

    public int bufferizeQueued(MeshGenerator meshGenerator, long endTime) {
        int count = 0;
        boolean bufferizing;
        do {
            bufferizing = bufferize(meshGenerator);
            if (bufferizing) {
                count++;
            }
        } while (bufferizing && count < ConstantGameSettings.BUFFERIZE_CHUNKS_PER_FRAME);
        gameState.setItem("bufferizingQueueSize", nonBufferizedChunks.size());
        return count;
    }

    public boolean bufferize(MeshGenerator meshGenerator) {
        if (!nonBufferizedChunks.isEmpty()) {
            MeshData meshData = nonBufferizedChunks.remove();
            if (meshData instanceof EntityMeshData entityMeshData) {
                entityMeshData.entity().setMesh(meshGenerator.bufferizeEntityMesh(entityMeshData));
                entityMeshDefinitionCache.put(entityMeshData.entity().getType().toString(), entityMeshData.entity().getMesh().getDefinition());
            } else if (meshData instanceof ChunkMeshData chunkMeshData) {
                ChunkMesh chunkMesh = meshGenerator.bufferizeChunkMesh(chunkMeshData);
                ClientWorldChunk clientWorldChunk = chunks.get(chunkMeshData.chunkPosition());
                if (clientWorldChunk == null) {
                    chunks.put(chunkMeshData.chunkPosition(), new ClientWorldChunk(chunkMesh));
                } else {
                    clientWorldChunk.setMesh(chunkMesh);
                }
                queuedChunks.remove(chunkMeshData.chunkPosition());
            }
            return true;
        }
        return false;
    }

    public void add(Position3D position3D, MeshData meshData) {
        ClientWorldChunk clientWorldChunk = chunks.get(position3D);
        if (clientWorldChunk == null) {
            chunks.put(position3D, new ClientWorldChunk(meshData));
        } else {
            clientWorldChunk.setMeshData(meshData);
        }
        nonBufferizedChunks.add(meshData);
        chunkResponseGotten.incrementAndGet();
        newChunks.add(position3D);
        gameState.setItem("shouldCheckNewChunks", true);
    }

    public void freeAll() {
        for (ClientWorldChunk clientWorldChunk : getValues()) {
            ChunkMesh chunkMesh = clientWorldChunk.getMesh();
            if (chunkMesh != null) {
                GL30C.glDeleteVertexArrays(chunkMesh.solidVAO());
                GL30C.glDeleteBuffers(chunkMesh.solidVBO());
                GL30C.glDeleteBuffers(chunkMesh.solidEBO());

                GL30C.glDeleteVertexArrays(chunkMesh.transparentVAO());
                GL30C.glDeleteBuffers(chunkMesh.transparentVBO());
                GL30C.glDeleteBuffers(chunkMesh.transparentEBO());
            }
        }
        chunks.clear();
    }

    public void tick() {
        int crs = chunkRequestsSent.get();
        int crg = chunkResponseGotten.get();
        int inflightRequests = crs - crg;
        gameState.setItem("inflight_requests", inflightRequests);
        gameState.setItem("chunk_requests_sent", crs);
        gameState.setItem("chunk_requests_received", crg);
        requesting = true;
//        missingChunks.clear()
    }

    public void freeAllChunksNotIn(Predicate<Position3D> predicate) {
        Position3D[] positions = getKeys();
        for (Position3D position : positions) {
            if (!predicate.test(position)) {
                ChunkMesh mesh = get(position, false).getMesh();
                if (mesh != null) {
                    GL30C.glDeleteVertexArrays(mesh.solidVAO());
                    GL30C.glDeleteBuffers(mesh.solidVBO());
                    GL30C.glDeleteBuffers(mesh.solidEBO());

                    GL30C.glDeleteVertexArrays(mesh.transparentVAO());
                    GL30C.glDeleteBuffers(mesh.transparentVBO());
                    GL30C.glDeleteBuffers(mesh.transparentEBO());

                    mesh.meshData().cleanup();
                }
            }
        }
    }

    public void addEntity(ClientEntity entity) {
        entities.put(entity.getUUID(), entity);
        nonBufferizedChunks.add(entity.getMeshData());
    }

    public Map<String, ClientEntity> getEntities() {
        return entities;
    }

    public IDCache<String, EntityMeshDataDefinition> getEntityMeshDefinitionCache() {
        return this.entityMeshDefinitionCache;
    }

    public Set<String> getQueuedEntityMeshData() {
        return this.queuedEntityMeshData;
    }

    public void removeEntity(String entityID) {
        entities.remove(entityID);
    }
}