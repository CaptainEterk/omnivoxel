package omnivoxel.server.entity;

import omnivoxel.server.client.ServerItem;
import omnivoxel.util.bytes.ByteUtils;

import java.nio.charset.StandardCharsets;

public record ServerEntityMesh(
        String id,
        String shapeID,
        String textureID,
        String[] childrenIDs
) implements ServerItem {
    @Override
    public byte[] getBytes() {
        byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);
        byte[] shapeBytes = shapeID.getBytes(StandardCharsets.UTF_8);
        byte[] textureBytes = textureID.getBytes(StandardCharsets.UTF_8);

        byte[][] childBytes = new byte[childrenIDs.length][];
        int childrenSize = 0;

        for (int i = 0; i < childrenIDs.length; i++) {
            childBytes[i] = childrenIDs[i].getBytes(StandardCharsets.UTF_8);
            childrenSize += Integer.BYTES + childBytes[i].length;
        }

        int size =
                Integer.BYTES + idBytes.length +
                        Integer.BYTES + shapeBytes.length +
                        Integer.BYTES + textureBytes.length +
                        Integer.BYTES +
                        childrenSize;

        byte[] bytes = new byte[size];
        int offset = 0;

        ByteUtils.addInt(bytes, idBytes.length, offset);
        offset += Integer.BYTES;
        System.arraycopy(idBytes, 0, bytes, offset, idBytes.length);
        offset += idBytes.length;

        ByteUtils.addInt(bytes, shapeBytes.length, offset);
        offset += Integer.BYTES;
        System.arraycopy(shapeBytes, 0, bytes, offset, shapeBytes.length);
        offset += shapeBytes.length;

        ByteUtils.addInt(bytes, textureBytes.length, offset);
        offset += Integer.BYTES;
        System.arraycopy(textureBytes, 0, bytes, offset, textureBytes.length);
        offset += textureBytes.length;

        ByteUtils.addInt(bytes, childrenIDs.length, offset);
        offset += Integer.BYTES;

        for (byte[] child : childBytes) {
            ByteUtils.addInt(bytes, child.length, offset);
            offset += Integer.BYTES;

            System.arraycopy(child, 0, bytes, offset, child.length);
            offset += child.length;
        }

        return bytes;
    }
}