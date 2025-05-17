package omnivoxel.client.game.world;

import omnivoxel.client.game.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.mesh.util.MeshGenerator;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.ChunkRequest;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.math.Position3D;
import omnivoxel.world.World;
import omnivoxel.world.chunk.Chunk;
import org.lwjgl.opengl.GL30C;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientWorld implements World<ClientWorldChunk> {
    private final Set<Position3D> queuedChunks;
    private final AtomicInteger chunkRequestsSent;
    private final AtomicInteger chunkResponseGotten;
    private final Map<Position3D, MeshData> nonBufferizedChunks;
    private final Map<Position3D, ClientWorldChunk> chunks;

    private final Set<Position3D> newChunks;

    private final GameState gameState;

    private Client client;

    public ClientWorld(GameState gameState) {
        this.gameState = gameState;
        queuedChunks = ConcurrentHashMap.newKeySet();
        nonBufferizedChunks = new ConcurrentHashMap<>();

        chunkRequestsSent = new AtomicInteger();
        chunkResponseGotten = new AtomicInteger();
        newChunks = ConcurrentHashMap.newKeySet();
        chunks = new ConcurrentHashMap<>();
    }

    @Override
    public void add(Position3D position3D, ClientWorldChunk clientWorldChunk) {
        throw new UnsupportedOperationException("Use a different add() method!");
    }

    public ClientWorldChunk get(Position3D position3D) {
        ClientWorldChunk clientWorldChunk = chunks.get(position3D);
        if (clientWorldChunk == null) {
            int inflightRequests = chunkRequestsSent.get() - chunkResponseGotten.get();
            if (inflightRequests < ConstantServerSettings.CHUNK_REQUEST_LIMIT && queuedChunks.add(position3D)) {
                client.sendRequest(new ChunkRequest(position3D));
                chunkRequestsSent.incrementAndGet();
            }
        }
        return clientWorldChunk;
    }

    public int bufferizeChunks(MeshGenerator meshGenerator, long endTime) {
        int count = 0;
        boolean bufferizing;
        do {
            bufferizing = bufferizeChunk(meshGenerator);
            if (bufferizing) {
                count++;
            }
        } while (bufferizing && count < ConstantGameSettings.BUFFERIZE_CHUNKS_PER_FRAME);
        gameState.setItem("bufferizingQueueSize", nonBufferizedChunks.size());
        return count;
    }

    public boolean bufferizeChunk(MeshGenerator meshGenerator) {
        if (!nonBufferizedChunks.isEmpty()) {
            Map.Entry<Position3D, MeshData> entry = nonBufferizedChunks.entrySet().iterator().next();
            nonBufferizedChunks.remove(entry.getKey());
            ChunkMesh chunkMesh = meshGenerator.bufferizeChunkMesh(entry.getValue());
            ClientWorldChunk clientWorldChunk = chunks.get(entry.getKey());
            if (clientWorldChunk == null) {
                chunks.put(entry.getKey(), new ClientWorldChunk(chunkMesh));
            } else {
                clientWorldChunk.setMesh(chunkMesh);
            }
            queuedChunks.remove(entry.getKey());
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
        nonBufferizedChunks.put(position3D, meshData);
        chunkResponseGotten.incrementAndGet();
        newChunks.add(position3D);
        gameState.setItem("shouldCheckNewChunks", true);
    }

    public void add(Position3D position3D, Chunk chunk) {
        ClientWorldChunk clientWorldChunk = chunks.get(position3D);
        if (clientWorldChunk == null) {
//            chunks.(position3D, new ClientWorldChunk(chunk));
        } else {
//            clientWorldChunk.setChunkData(chunk);
        }
    }

    public void freeAll() {
        for (ClientWorldChunk clientWorldChunk : chunks.values()) {
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

    public void freeAllChunksNotIn(List<Position3D> chunks) {
        if (this.chunks.size() == chunks.size()) {
            return;
        }

        int inflightRequests = chunkRequestsSent.get() - chunkResponseGotten.get();
        System.out.printf("inflight: %d, gotten: %d, sent: %d\n", inflightRequests, chunkResponseGotten.get(), chunkRequestsSent.get());

        for (Map.Entry<Position3D, ClientWorldChunk> entry : this.chunks.entrySet()) {
            Position3D position3D = entry.getKey();

            if (!chunks.remove(position3D)) {
                ChunkMesh chunkMesh = entry.getValue().getMesh();
                if (chunkMesh != null) {
                    GL30C.glDeleteVertexArrays(chunkMesh.solidVAO());
                    GL30C.glDeleteBuffers(chunkMesh.solidVBO());
                    GL30C.glDeleteBuffers(chunkMesh.solidEBO());

                    GL30C.glDeleteVertexArrays(chunkMesh.transparentVAO());
                    GL30C.glDeleteBuffers(chunkMesh.transparentVBO());
                    GL30C.glDeleteBuffers(chunkMesh.transparentEBO());

                    this.chunks.remove(position3D);
                }
            }
        }
    }

    public Set<Position3D> getNewChunks() {
        return newChunks;
    }

    public int totalQueuedChunks() {
        return queuedChunks.size();
    }

    public void setClient(Client client) {
        this.client = client;
    }
}