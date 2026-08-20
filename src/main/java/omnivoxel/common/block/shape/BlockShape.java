package omnivoxel.common.block.shape;

import omnivoxel.server.client.ServerItem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record BlockShape(String id, BlockVertex[][] vertices, int[][] indices, boolean[] solid, boolean[] coverable, boolean[] coversOppositeSelfFace) implements ServerItem {
    public static final String DEFAULT_BLOCK_SHAPE_STRING = "omnivoxel:default_block_shape";
    public static final String EMPTY_BLOCK_SHAPE_STRING = "omnivoxel:empty_block_shape";
    // Default unit cube vertices
    private static final BlockVertex[][] CUBE_VERTICES = {
            // Top (+Y)
            {
                    new BlockVertex(0, 1, 0),
                    new BlockVertex(0, 1, 1),
                    new BlockVertex(1, 1, 1),
                    new BlockVertex(1, 1, 0)
            },
            // Bottom (-Y)
            {
                    new BlockVertex(0, 0, 0),
                    new BlockVertex(1, 0, 0),
                    new BlockVertex(1, 0, 1),
                    new BlockVertex(0, 0, 1)
            },
            // North (+Z)
            {
                    new BlockVertex(0, 0, 1),
                    new BlockVertex(1, 0, 1),
                    new BlockVertex(1, 1, 1),
                    new BlockVertex(0, 1, 1)
            },
            // South (-Z)
            {
                    new BlockVertex(0, 1, 0),
                    new BlockVertex(1, 1, 0),
                    new BlockVertex(1, 0, 0),
                    new BlockVertex(0, 0, 0)
            },
            // East (+X)
            {
                    new BlockVertex(1, 1, 0),
                    new BlockVertex(1, 1, 1),
                    new BlockVertex(1, 0, 1),
                    new BlockVertex(1, 0, 0)
            },
            // West (-X)
            {
                    new BlockVertex(0, 0, 0),
                    new BlockVertex(0, 0, 1),
                    new BlockVertex(0, 1, 1),
                    new BlockVertex(0, 1, 0)
            }
    };
    private static final int[][] CUBE_INDICES = {
            {0, 1, 2, 2, 3, 0},
            {0, 1, 2, 2, 3, 0},
            {0, 1, 2, 2, 3, 0},
            {0, 1, 2, 2, 3, 0},
            {0, 1, 2, 2, 3, 0},
            {0, 1, 2, 2, 3, 0},
    };
    private static final boolean[] CUBE_SOLID = {true, true, true, true, true, true};
    private static final boolean[] CUBE_COVERABLE = {true, true, true, true, true, true};
    private static final boolean[] CUBE_COVERS_OPPOSITE_SELF_FACE = {true, true, true, true, true, true};
    public static final BlockShape DEFAULT_BLOCK_SHAPE = new BlockShape(BlockShape.DEFAULT_BLOCK_SHAPE_STRING, CUBE_VERTICES, CUBE_INDICES, CUBE_SOLID, CUBE_COVERABLE, CUBE_COVERS_OPPOSITE_SELF_FACE);
    private static final BlockVertex[][] EMPTY_VERTICES = new BlockVertex[6][0];
    private static final int[][] EMPTY_INDICES = new int[6][0];
    private static final boolean[] EMPTY_SOLID = new boolean[6];
    private static final boolean[] EMPTY_COVERABLE = new boolean[6];
    private static final boolean[] EMPTY_COVERS_OPPOSITE_SELF_FACE = new boolean[6];
    public static final BlockShape EMPTY_BLOCK_SHAPE =
            new BlockShape(BlockShape.EMPTY_BLOCK_SHAPE_STRING, EMPTY_VERTICES, EMPTY_INDICES, EMPTY_SOLID, EMPTY_COVERABLE, EMPTY_COVERS_OPPOSITE_SELF_FACE);

    @Override
    public byte[] getBytes() {
        byte[] idBytes = id == null ? new byte[0] : id.getBytes(StandardCharsets.UTF_8);
        int idLen = idBytes.length;

        int capacity = 2 + idLen;
        for (int face = 0; face < 6; face++) {
            capacity += Short.BYTES;
            capacity += vertices[face].length * (3 * Float.BYTES);
            capacity += Short.BYTES;
            capacity += indices[face].length * Integer.BYTES;
            capacity += 3;
        }

        ByteBuffer buffer = ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN);

        buffer.putShort((short) idLen);
        buffer.put(idBytes);

        for (int face = 0; face < 6; face++) {
            buffer.putShort((short) vertices[face].length);
            for (BlockVertex v : vertices[face]) {
                buffer.putFloat(v.px());
                buffer.putFloat(v.py());
                buffer.putFloat(v.pz());
            }
            buffer.putShort((short) indices[face].length);
            for (int idx : indices[face]) {
                buffer.putInt(idx);
            }

            buffer.put((byte) (solid[face] ? 1 : 0));
            buffer.put((byte) (coverable[face] ? 1 : 0));
            buffer.put((byte) (coversOppositeSelfFace[face] ? 1 : 0));
        }

        return buffer.array();
    }
}