package omnivoxel.client.game.graphics.opengl.mesh.generators;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.ChunkMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.MeshDataTask;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.util.cache.IDCache;
import omnivoxel.util.math.Position3D;

import java.util.Set;

public final class MeshDataGenerator {
    private final ChunkMeshDataGenerator chunkMeshDataGenerator;
    private final EntityMeshDataGenerator entityMeshDataGenerator;
    private final ClientWorld world;

    public MeshDataGenerator(ClientWorldDataService worldDataService, IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<String> queuedEntityMeshData, ClientWorld world) {
        chunkMeshDataGenerator = new ChunkMeshDataGenerator(worldDataService);
        this.world = world;
        entityMeshDataGenerator = new EntityMeshDataGenerator(entityMeshDefinitionCache, queuedEntityMeshData);
    }

    public void generateMeshData(MeshDataTask meshDataTask) {
        try {
            if (meshDataTask instanceof ChunkMeshDataTask(ByteBuf blocks, Position3D position3D)) {
                world.add(position3D, chunkMeshDataGenerator.generateMeshData(blocks, position3D, world));
            } else if (meshDataTask instanceof EntityMeshDataTask(ClientEntity entity)) {
                world.addEntity(entityMeshDataGenerator.generateMeshData(entity));
            }
        } finally {
            meshDataTask.cleanup();
        }
    }
}