package omnivoxel.server.entity;

import omnivoxel.server.client.ServerItem;
import omnivoxel.util.bytes.ByteUtils;

public record ServerEntityMesh(ServerEntityShape serverEntityShape, ServerEntityTexture serverEntityTexture,
                               ServerEntityMesh[] children) implements ServerItem {
    @Override
    public byte[] getBytes() {
        byte[] shapeBytes = serverEntityShape.getBytes();
        byte[] textureBytes = serverEntityTexture.getBytes();
        byte[] bytes = new byte[shapeBytes.length + textureBytes.length + Integer.BYTES * 2];
        ByteUtils.addInt(bytes, shapeBytes.length, 0);
        System.arraycopy(shapeBytes, 0, bytes, Integer.BYTES, shapeBytes.length);
        ByteUtils.addInt(bytes, textureBytes.length, shapeBytes.length + Integer.BYTES);
        System.arraycopy(textureBytes, 0, bytes, textureBytes.length + Integer.BYTES * 2, textureBytes.length);
        // TODO: Add children
        return bytes;
    }
}