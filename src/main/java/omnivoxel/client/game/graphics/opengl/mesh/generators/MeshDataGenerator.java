package omnivoxel.client.game.graphics.opengl.mesh.generators;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.graphics.opengl.mesh.ChunkMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.MeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.math.Position3D;
import omnivoxel.server.client.entity.Entity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MeshDataGenerator {
    private final BiConsumer<Position3D, MeshData> loadChunk;
    private final Consumer<Entity> loadEntity;
    private final ChunkMeshDataGenerator chunkMeshDataGenerator;
    private final EntityMeshDataGenerator entityMeshDataGenerator;

    public MeshDataGenerator(BiConsumer<Position3D, MeshData> loadChunk, Consumer<Entity> loadEntity, ClientWorldDataService worldDataService) {
        this.loadChunk = loadChunk;
        this.loadEntity = loadEntity;
        chunkMeshDataGenerator = new ChunkMeshDataGenerator(worldDataService);
        entityMeshDataGenerator = new EntityMeshDataGenerator();
    }

    public void generateMeshData(MeshDataTask meshDataTask) {
        try {
            if (meshDataTask instanceof ChunkMeshDataTask(ByteBuf blocks, Position3D position3D)) {
                loadChunk.accept(position3D, chunkMeshDataGenerator.generateMeshData(blocks, position3D));
            } else if (meshDataTask instanceof EntityMeshDataTask(Entity entity)) {
                loadEntity.accept(entityMeshDataGenerator.generateMeshData(entity));
            }
        } finally {
            meshDataTask.cleanup();
        }
    }
}