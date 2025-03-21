package omnivoxel.server.client.block;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public record ServerBlock(String id, int... blockState) implements Block {
    public ServerBlock(String id) {
        this(id, new int[0]);
    }

    @Override
    public byte @NotNull [] getBytes() {
        byte[] idBytes = id == null ? new byte[0] : id.getBytes();

        byte[] stateBytes = new byte[blockState.length * Integer.BYTES];
        for (int i = 0; i < blockState.length; i++) {
            int index = i * Integer.BYTES;
            stateBytes[index] = (byte) ((blockState[i] >> 24) & 0xFF);
            stateBytes[index + 1] = (byte) ((blockState[i] >> 16) & 0xFF);
            stateBytes[index + 2] = (byte) ((blockState[i] >> 8) & 0xFF);
            stateBytes[index + 3] = (byte) (blockState[i] & 0xFF);
        }

        byte[] bytes = new byte[2 + idBytes.length + 2 + stateBytes.length];
        bytes[0] = (byte) ((idBytes.length >> 8) & 0xFF);
        bytes[1] = (byte) (idBytes.length & 0xFF);
        System.arraycopy(idBytes, 0, bytes, 2, idBytes.length);
        bytes[idBytes.length + 2] = (byte) ((blockState.length >> 8) & 0xFF);
        bytes[idBytes.length + 3] = (byte) (blockState.length & 0xFF);
        System.arraycopy(stateBytes, 0, bytes, idBytes.length + 4, stateBytes.length);

        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerBlock that = (ServerBlock) o;
        return Arrays.equals(blockState, that.blockState) && Objects.equals(id, that.id);
    }
}