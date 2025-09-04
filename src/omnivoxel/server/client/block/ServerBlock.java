package omnivoxel.server.client.block;

import omnivoxel.common.BlockShape;
import omnivoxel.server.client.ServerItem;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;

public record ServerBlock(String id, String blockState, String blockShape, boolean transparent) implements ServerItem {
    public ServerBlock(String id, boolean transparent) {
        this(id, null, BlockShape.DEFAULT_BLOCK_SHAPE_STRING, transparent);
    }

    public ServerBlock(String id, String blockShape, boolean transparent) {
        this(id, null, blockShape, transparent);
    }

    @Override
    public byte @NotNull [] getBytes() {
        String idAndState = id + ":" + blockState;
        byte[] idBytes = idAndState.getBytes();
        byte[] shapeBytes = blockShape == null ? new byte[0] : blockShape.getBytes();

        int size = 2 + idBytes.length
                + 2 + shapeBytes.length
                + 1;

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort((short) idBytes.length);
        buffer.put(idBytes);

        buffer.putShort((short) shapeBytes.length);
        buffer.put(shapeBytes);

        buffer.put((byte) (transparent ? 1 : 0));

        return buffer.array();
    }

    public byte[] getBlockBytes() {
        String idAndState = id + ":" + blockState;
        byte[] idBytes = idAndState.getBytes();

        int size = 2 + idBytes.length
                + 1;

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putShort((short) idBytes.length);
        buffer.put(idBytes);

        buffer.put((byte) (transparent ? 1 : 0));

        return buffer.array();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerBlock that = (ServerBlock) o;
        return Objects.equals(blockState, that.blockState) && Objects.equals(id, that.id);
    }
}