package omnivoxel.client.network.util;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;
import omnivoxel.common.BlockShape;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ByteBufUtils {
    private static final Map<String, BlockShape> shapeCache = new HashMap<>();

    public static BlockShape readBlockShapeFromByteBuf(ByteBuf byteBuf) {
        // --- Read key ---
        byteBuf.skipBytes(8);
        int idLen = byteBuf.readUnsignedShort();
        byte[] idBytes = new byte[idLen];
        byteBuf.readBytes(idBytes);
        String id = new String(idBytes, StandardCharsets.UTF_8);

        // --- Read faces ---
        Vertex[][] vertices = new Vertex[6][];
        int[][] indices = new int[6][];
        boolean[] solid = new boolean[6];

        for (int face = 0; face < 6; face++) {
            // vertices
            int vCount = byteBuf.readUnsignedShort();
            Vertex[] verts = new Vertex[vCount];
            for (int i = 0; i < vCount; i++) {
                float x = byteBuf.readFloat();
                float y = byteBuf.readFloat();
                float z = byteBuf.readFloat();
                verts[i] = new Vertex(x, y, z);
            }
            vertices[face] = verts;

            // indices
            int iCount = byteBuf.readUnsignedShort();
            int[] idx = new int[iCount];
            for (int i = 0; i < iCount; i++) {
                idx[i] = byteBuf.readInt();
            }
            indices[face] = idx;

            // solid
            solid[face] = byteBuf.readByte() != 0;
        }

        return new BlockShape(id, vertices, indices, solid);
    }

    public static Block registerBlockFromByteBuf(ByteBuf byteBuf) {
        int readerIndex = 8;

        int idLength = byteBuf.getShort(readerIndex);
        readerIndex += 2;

        byte[] idBytes = new byte[idLength];
        byteBuf.getBytes(readerIndex, idBytes);
        readerIndex += idLength;

        String blockIDState = new String(idBytes);

        String[] ids = blockIDState.split(":");
        String modID = ids[0] + ":" + ids[1];

        final String blockID = modID.contains(":") ? modID.split(":", 2)[1] : modID;

        int shapeIDLength = byteBuf.getShort(readerIndex);
        readerIndex += 2;

        byte[] shapeIDBytes = new byte[shapeIDLength];
        byteBuf.getBytes(readerIndex, shapeIDBytes);
        readerIndex += shapeIDLength;

        final String shapeID = new String(shapeIDBytes);
        final BlockShape blockShape = shapeCache.getOrDefault(shapeID, BlockShape.DEFAULT_BLOCK_SHAPE);

        boolean transparent = byteBuf.getByte(readerIndex) == 1;

        shapeCache.put(blockShape.id(), blockShape);

        return new Block() {
            @Override
            public String getID() {
                return blockID;
            }

            @Override
            public String getModID() {
                return modID;
            }

            @Override
            public BlockShape getShape(Block top, Block bottom, Block north, Block south, Block east, Block west) {
                return blockShape;
            }

            @Override
            public boolean shouldRenderFace(BlockFace face, Block adjacentBlock) {
                if (modID.equals("omnivoxel:air") || adjacentBlock.getModID().equals(modID) || !adjacentBlock.isTransparent())
                    return false;
                if (transparent) return true;
                return true;
            }

            @Override
            public int[] getUVCoordinates(BlockFace blockFace) {
                return new int[]{2, 0, 3, 0, 3, 1, 2, 1};
            }

            @Override
            public boolean isTransparent() {
                return transparent;
            }

            @Override
            public boolean shouldRenderTransparentMesh() {
                return transparent;
            }
        };
    }
}