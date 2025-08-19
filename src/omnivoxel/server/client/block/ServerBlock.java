package omnivoxel.server.client.block;

import omnivoxel.common.BlockShape;
import omnivoxel.server.client.ServerItem;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public record ServerBlock(String id, int[] blockState, String blockShape, boolean transparent) implements ServerItem {
    public ServerBlock(String id, boolean transparent) {
        this(id, null, BlockShape.DEFAULT_BLOCK_SHAPE_STRING, transparent);
    }

    public ServerBlock(String id, String blockShape, boolean transparent) {
        this(id, null, blockShape, transparent);
    }

    @Override
    public byte @NotNull [] getBytes() {
        byte[] idBytes = id == null ? new byte[0] : id.getBytes();
        byte[] shapeBytes = blockShape == null ? new byte[0] : blockShape.getBytes();

        int stateLength = blockState != null ? blockState.length : 0;

        int size = 2 + idBytes.length
                + 2 + (stateLength * 4)
                + 2 + shapeBytes.length
                + 1;

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort((short) idBytes.length);
        buffer.put(idBytes);

        buffer.putShort((short) stateLength);
        if (blockState != null) {
            for (int s : blockState) buffer.putInt(s);
        }

        buffer.putShort((short) shapeBytes.length);
        buffer.put(shapeBytes);

        buffer.put((byte) (transparent ? 1 : 0));

        return buffer.array();
    }

    public byte[] getBlockBytes() {
        byte[] idBytes = id == null ? new byte[0] : id.getBytes();
        int stateLength = blockState != null ? blockState.length : 0;

        int size = 2 + idBytes.length
                + 2 + (stateLength * 4)
                + 1;

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort((short) idBytes.length);
        buffer.put(idBytes);

        buffer.putShort((short) stateLength);
        if (blockState != null) {
            for (int s : blockState) buffer.putInt(s);
        }

        buffer.put((byte) (transparent ? 1 : 0));

        return buffer.array();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerBlock that = (ServerBlock) o;
        return Arrays.equals(blockState, that.blockState) && Objects.equals(id, that.id);
    }
}