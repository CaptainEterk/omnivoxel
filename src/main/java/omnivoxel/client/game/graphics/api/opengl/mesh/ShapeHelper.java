package omnivoxel.client.game.graphics.api.opengl.mesh;

import omnivoxel.common.block.shape.BlockVertex;
import omnivoxel.common.settings.ConstantCommonSettings;

public class ShapeHelper {
    private static final int POSITION_SCALE = 512 / ConstantCommonSettings.CHUNK_SIZE;
    private static final int BITMASK_3 = 0x7;
    private static final int BITMASK_8 = 0xFF;
    private static final int BITMASK_6 = 0x3F;
    private static final int BITMASK_12 = 0xFFF;

    public static void packVertexData(BlockVertex vertex, int r, int g, int b, int s, int blockFace, int u, int v, boolean loose, int type, int[] vertexData) {
        vertexData[0] = ((int) (vertex.px() * POSITION_SCALE) << 22) | ((int) (vertex.py() * POSITION_SCALE) << 12) | ((int) (vertex.pz() * POSITION_SCALE) << 2);
        vertexData[1] = ((blockFace & BITMASK_3) << 29)
                | ((u & BITMASK_8) << 21)
                | ((v & BITMASK_8) << 13)
                | ((loose ? 1 : 0) << 12)
                | (type & BITMASK_12);
        vertexData[2] = ((r & BITMASK_6) << 28)
                | ((g & BITMASK_6) << 24)
                | ((b & BITMASK_6) << 20)
                | ((s & BITMASK_6) << 16);
    }
}