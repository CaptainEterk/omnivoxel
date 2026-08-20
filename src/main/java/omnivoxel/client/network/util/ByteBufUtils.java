package omnivoxel.client.network.util;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.graphics.block.BlockMesh;
import omnivoxel.client.game.graphics.light.channel.LightChannels;
import omnivoxel.common.block.hitbox.BlockHitbox;
import omnivoxel.common.block.shape.BlockShape;
import omnivoxel.common.block.shape.BlockVertex;
import omnivoxel.common.face.BlockFace;
import omnivoxel.util.log.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ByteBufUtils {
    private static final Map<String, BlockShape> shapeCache = new HashMap<>();
    private static final Map<String, BlockHitbox[]> hitboxCache = new HashMap<>();

    public static void cacheBlockShapeFromByteBuf(ByteBuf byteBuf) {
        byteBuf.readerIndex(8);
        int idLen = byteBuf.readUnsignedShort();
        byte[] idBytes = new byte[idLen];
        byteBuf.readBytes(idBytes);
        String id = new String(idBytes, StandardCharsets.UTF_8);

        BlockVertex[][] vertices = new BlockVertex[6][];
        int[][] indices = new int[6][];
        boolean[] solid = new boolean[6];
        boolean[] coverable = new boolean[6];
        boolean[] coversOppositeSelfFace = new boolean[6];

        for (int face = 0; face < 6; face++) {
            int vCount = byteBuf.readUnsignedShort();
            BlockVertex[] vertexArray = new BlockVertex[vCount];
            for (int i = 0; i < vCount; i++) {
                float x = byteBuf.readFloat();
                float y = byteBuf.readFloat();
                float z = byteBuf.readFloat();
                vertexArray[i] = new BlockVertex(x, y, z);
            }
            vertices[face] = vertexArray;

            int iCount = byteBuf.readUnsignedShort();
            int[] idx = new int[iCount];
            for (int i = 0; i < iCount; i++) {
                idx[i] = byteBuf.readInt();
            }
            indices[face] = idx;

            solid[face] = byteBuf.readByte() != 0;
            coverable[face] = byteBuf.readByte() != 0;
            coversOppositeSelfFace[face] = byteBuf.readByte() != 0;
        }

        BlockShape blockShape = new BlockShape(id, vertices, indices, solid, coverable, coversOppositeSelfFace);

        shapeCache.put(id, blockShape);
    }

    public static void cacheBlockHitboxFromByteBuf(ByteBuf byteBuf) {
        byteBuf.readerIndex(8);
        int idLen = byteBuf.readInt();
        byte[] idBytes = new byte[idLen];
        byteBuf.readBytes(idBytes);
        String id = new String(idBytes, StandardCharsets.UTF_8);

        int hitboxCount = byteBuf.readInt();
        BlockHitbox[] hitboxes = new BlockHitbox[hitboxCount];
        for (int i = 0; i < hitboxCount; i++) {
            float minX = byteBuf.readFloat();
            float minY = byteBuf.readFloat();
            float minZ = byteBuf.readFloat();

            float maxX = byteBuf.readFloat();
            float maxY = byteBuf.readFloat();
            float maxZ = byteBuf.readFloat();

            boolean isVolume = byteBuf.readBoolean();
            boolean isGround = byteBuf.readBoolean();
            float gravity = byteBuf.readFloat();

            hitboxes[i] = new BlockHitbox(minX, minY, minZ, maxX, maxY, maxZ, new BlockHitbox.BlockHitboxVolumeProperties(isVolume, gravity, isGround));
        }

        Logger.info("Registering block hitbox: " + id);

        hitboxCache.put(id, hitboxes);
    }

    public static BlockMesh registerBlockFromByteBuf(ByteBuf byteBuf) {
        int readerIndex = 8;

        int idLength = byteBuf.getShort(readerIndex);
        readerIndex += 2;

        byte[] idBytes = new byte[idLength];
        byteBuf.getBytes(readerIndex, idBytes);
        readerIndex += idLength;

        String blockIDState = new String(idBytes);

        String[] ids = blockIDState.split("/");
        if (ids.length == 1) {
            ids = new String[]{ids[0], "default"};
        }
        String modID = ids[0];

        int shapeIDLength = byteBuf.getShort(readerIndex);
        readerIndex += 2;

        byte[] shapeIDBytes = new byte[shapeIDLength];
        byteBuf.getBytes(readerIndex, shapeIDBytes);
        readerIndex += shapeIDLength;

        final String shapeID = new String(shapeIDBytes);
        final BlockShape blockShape = shapeCache.getOrDefault(shapeID, BlockShape.DEFAULT_BLOCK_SHAPE);

        int hitboxIDLength = byteBuf.getShort(readerIndex);

        readerIndex += 2;
        byte[] hitboxIDBytes = new byte[hitboxIDLength];
        byteBuf.getBytes(readerIndex, hitboxIDBytes);
        readerIndex += hitboxIDLength;

        final String hitboxID = new String(hitboxIDBytes);
        final BlockHitbox[] hitbox = hitboxCache.getOrDefault(hitboxID, BlockHitbox.EMPTY_BLOCK_HITBOX);
        hitboxCache.put(hitboxID, hitbox);

        boolean transparentMesh = byteBuf.getByte(readerIndex++) == 1;
        boolean decorationMesh = byteBuf.getByte(readerIndex++) == 1;
        boolean isSelfOccluded = byteBuf.getByte(readerIndex++) == 1;
        boolean rotatable = byteBuf.getByte(readerIndex++) == 1;
        boolean canPlaceOn = byteBuf.getByte(readerIndex++) == 1;

        int[][] allUVCoords = new int[6][];
        for (int f = 0; f < 6; f++) {
            short uvCoordCount = byteBuf.getShort(readerIndex);
            readerIndex += 2;
            int[] uvCoords = new int[uvCoordCount];
            for (int i = 0; i < uvCoordCount; i++) {
                uvCoords[i] = (int) byteBuf.getDouble(readerIndex);
                readerIndex += Double.BYTES;
            }
            allUVCoords[f] = uvCoords;
        }

        byte[][] lightEmitting = new byte[6][3];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                lightEmitting[i][j] = byteBuf.getByte(readerIndex++);
            }
        }

        byte[][] lightDiffusing = new byte[6][4];

        for (int i = 0; i < lightDiffusing.length; i++) {
            for (int j = 0; j < 4; j++) {
                lightDiffusing[i][j] = byteBuf.getByte(readerIndex++);
            }
        }

        Logger.info("Registering block: " + blockIDState + " " + Arrays.deepToString(lightEmitting) + " " + Arrays.deepToString(lightDiffusing));

        shapeCache.put(blockShape.id(), blockShape);

        return new BlockMesh(ids[1]) {
            @Override
            public String getModID() {
                return modID;
            }

            // TODO: Add rules for block shape
            @Override
            public BlockShape getShape() {
                return blockShape;
            }

            @Override
            public BlockHitbox[] getHitbox() {
                return hitbox;
            }

            @Override
            public int[] getUVCoordinates(BlockFace blockFace) {
                return allUVCoords[blockFace.ordinal()];
            }

            @Override
            public byte getLightDiffuse(BlockFace blockFace, LightChannels channel) {
                return lightDiffusing[blockFace.ordinal()][channel.ordinal()];
            }

            @Override
            public byte getLightEmitting(BlockFace blockFace, LightChannels channel) {
                return lightEmitting[blockFace.ordinal()][channel.ordinal()];
            }

            @Override
            public boolean shouldRenderTransparentMesh() {
                return transparentMesh;
            }

            @Override
            public boolean shouldRenderDecorationMesh() {
                return decorationMesh;
            }

            @Override
            public boolean isSelfOccluded() {
                return isSelfOccluded;
            }

            @Override
            public boolean isRotatable() {
                return rotatable;
            }

            @Override
            public boolean canPlaceOn() {
                return canPlaceOn;
            }

            @Override
            public int getShaderType() {
                // TODO: Add this, but it also needs to run with dynamic shaders, so it needs to be generated at runtime, maybe with a boolean flag in-game called "enable_shaders" or something
                return 0;
            }
        };
    }
}
