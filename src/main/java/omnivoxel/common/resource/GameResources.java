package omnivoxel.common.resource;

import omnivoxel.server.client.ServerItem;
import omnivoxel.server.entity.ServerEntityMesh;
import omnivoxel.server.entity.ServerEntityShape;
import omnivoxel.server.entity.ServerEntityTexture;
import omnivoxel.util.bytes.ByteUtils;

import java.util.Map;

public record GameResources(Map<String, ServerEntityShape> serverEntityShapes,
                            Map<String, ServerEntityTexture> serverEntityTextures,
                            Map<String, ServerEntityMesh> serverEntityMeshes) implements ServerItem {
    public ServerEntityMesh getEntityMesh(String meshID) {
        return serverEntityMeshes.get(meshID);
    }

    @Override
    public byte[] getBytes() {
        byte[][] shapeBytes = new byte[serverEntityShapes.size()][];
        byte[][] textureBytes = new byte[serverEntityTextures.size()][];
        byte[][] meshBytes = new byte[serverEntityMeshes.size()][];

        int size = Integer.BYTES * 3;

        int i = 0;
        for (ServerEntityShape shape : serverEntityShapes.values()) {
            shapeBytes[i] = shape.getBytes();
            size += shapeBytes[i].length;
            i++;
        }

        i = 0;
        for (ServerEntityTexture texture : serverEntityTextures.values()) {
            textureBytes[i] = texture.getBytes();
            size += textureBytes[i].length;
            i++;
        }

        i = 0;
        for (ServerEntityMesh mesh : serverEntityMeshes.values()) {
            meshBytes[i] = mesh.getBytes();
            size += meshBytes[i].length;
            i++;
        }

        byte[] out = new byte[size];
        int offset = 0;

        ByteUtils.addInt(out, shapeBytes.length, offset);
        offset += Integer.BYTES;

        for (byte[] bytes : shapeBytes) {
            System.arraycopy(bytes, 0, out, offset, bytes.length);
            offset += bytes.length;
        }

        ByteUtils.addInt(out, textureBytes.length, offset);
        offset += Integer.BYTES;

        for (byte[] bytes : textureBytes) {
            System.arraycopy(bytes, 0, out, offset, bytes.length);
            offset += bytes.length;
        }

        ByteUtils.addInt(out, meshBytes.length, offset);
        offset += Integer.BYTES;

        for (byte[] bytes : meshBytes) {
            System.arraycopy(bytes, 0, out, offset, bytes.length);
            offset += bytes.length;
        }

        return out;
    }
}