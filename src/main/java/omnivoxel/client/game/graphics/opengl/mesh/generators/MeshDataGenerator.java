package omnivoxel.client.game.graphics.opengl.mesh.generators;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.ChunkMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.MeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.math.Position3D;
import omnivoxel.util.cache.IDCache;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MeshDataGenerator {
    private final BiConsumer<Position3D, MeshData> loadChunk;
    private final Consumer<ClientEntity> loadEntity;
    private final ChunkMeshDataGenerator chunkMeshDataGenerator;
    private final EntityMeshDataGenerator entityMeshDataGenerator;

    public MeshDataGenerator(BiConsumer<Position3D, MeshData> loadChunk, Consumer<ClientEntity> loadEntity, ClientWorldDataService worldDataService, IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<String> queuedEntityMeshData) {
        this.loadChunk = loadChunk;
        this.loadEntity = loadEntity;
        chunkMeshDataGenerator = new ChunkMeshDataGenerator(worldDataService);
        entityMeshDataGenerator = new EntityMeshDataGenerator(entityMeshDefinitionCache, queuedEntityMeshData);
    }

    public void generateMeshData(MeshDataTask meshDataTask) {
        try {
            if (meshDataTask instanceof ChunkMeshDataTask(ByteBuf blocks, Position3D position3D)) {
                loadChunk.accept(position3D, chunkMeshDataGenerator.generateMeshData(blocks, position3D));
            } else if (meshDataTask instanceof EntityMeshDataTask(ClientEntity entity)) {
                loadEntity.accept(entityMeshDataGenerator.generateMeshData(entity));
            }
        } finally {
            meshDataTask.cleanup();
        }
    }
}