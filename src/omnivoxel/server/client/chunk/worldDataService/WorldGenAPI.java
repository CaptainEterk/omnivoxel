package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;
import omnivoxel.common.BlockShape;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.server.client.chunk.worldDataService.density.functions.Noise3DDensityFunction;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.Map;

public class WorldGenAPI {
    private final int seed;
    private final Map<String, BlockShape> blockShapeCache;
    private final ServerBlockService serverBlockService;

    public WorldGenAPI(int seed, Map<String, BlockShape> blockShapeCache, ServerBlockService serverBlockService) {
        this.seed = seed;
        this.serverBlockService = serverBlockService;
        this.blockShapeCache = blockShapeCache;
    }

    @HostAccess.Export
    public void registerBlock(Value block) {
        String id = block.getMember("id").as(String.class);
        String blockShapeID = block.hasMember("block_shape") ? block.getMember("block_shape").as(String.class) : BlockShape.DEFAULT_BLOCK_SHAPE_STRING;
        boolean transparent = block.hasMember("transparent") ? block.getMember("transparent").as(boolean.class) : false;
        serverBlockService.addServerBlock(id, new ServerBlock(id, blockShapeID, transparent));
    }

    @HostAccess.Export
    public void registerBlockShape(String id, float[][][] vertices, int[][] indices, boolean[] solid) {
        Vertex[][] verts = new Vertex[6][];
        for (int i = 0; i < 6; i++) {
            Vertex[] vs = new Vertex[vertices[i].length];
            for (int j = 0; j < vertices[i].length; j++) {
                vs[j] = new Vertex(vertices[i][j][0], vertices[i][j][1], vertices[i][j][2]);
            }
            verts[i] = vs;
        }
        blockShapeCache.put(id, new BlockShape(id, verts, indices, solid));
    }

    @HostAccess.Export
    public void registerNoise(String id, double[] amplitudes, double firstOctave) {
        Noise3DDensityFunction.noises.put(id, new Noise3D(amplitudes, firstOctave, seed));
    }
}