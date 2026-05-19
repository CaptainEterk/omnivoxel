package omnivoxel.server.io.entity;

import omnivoxel.common.settings.ConstantServerSettings;
import omnivoxel.server.entity.Entity;
import omnivoxel.server.entity.EntityType;
import omnivoxel.server.entity.mob.MobEntity;
import omnivoxel.util.math.Position3D;
import omnivoxel.util.bytes.ByteUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class EntityIO {
    private static final String ENTITY_EXT = ".entities";

    public static byte[] get(Position3D position3D) throws IOException {
        Path path = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION + position3D.getPath().replace(".chunk", ENTITY_EXT));
        return Files.exists(path) ? Files.readAllBytes(path) : null;
    }

    public static void writeEntities(Position3D position3D, Set<Entity> entities) throws IOException {
        if (entities == null) return;
        // Simple binary format:
        // int entityCount
        // for each entity:
        //   int idLength (should be 32)
        //   id bytes
        //   int typeOrdinal
        //   double x,y,z,pitch,yaw
        //   int nameLength
        //   name bytes (utf-8)

        int total = Integer.BYTES; // count
        byte[][] encoded = new byte[entities.size()][];
        int idx = 0;
        for (Entity e : entities) {
            // entity id is not accessible from here; write zero-length id
            byte[] idBytes = new byte[0];

            int nameLen = 0;
            byte[] nameBytes = new byte[0];
            int typeOrdinal = 0;
            double x = 0, y = 0, z = 0, pitch = 0, yaw = 0;
            if (e instanceof MobEntity me) {
                typeOrdinal = 1; // PLAYER default for mob-like
                String name = me.getName();
                if (name != null) {
                    nameBytes = name.getBytes();
                    nameLen = nameBytes.length;
                }
                x = me.getX();
                y = me.getY();
                z = me.getZ();
                pitch = me.getPitch();
                yaw = me.getYaw();
            }

            int size = Integer.BYTES + idBytes.length + Integer.BYTES + Double.BYTES * 5 + Integer.BYTES + nameLen;
            byte[] out = new byte[size];
            int off = 0;
            ByteUtils.addInt(out, idBytes.length, off);
            off += Integer.BYTES;
            System.arraycopy(idBytes, 0, out, off, idBytes.length);
            off += idBytes.length;

            ByteUtils.addInt(out, typeOrdinal, off);
            off += Integer.BYTES;

            ByteUtils.addDouble(out, x, off);
            off += Double.BYTES;
            ByteUtils.addDouble(out, y, off);
            off += Double.BYTES;
            ByteUtils.addDouble(out, z, off);
            off += Double.BYTES;
            ByteUtils.addDouble(out, pitch, off);
            off += Double.BYTES;
            ByteUtils.addDouble(out, yaw, off);
            off += Double.BYTES;

            ByteUtils.addInt(out, nameLen, off);
            off += Integer.BYTES;
            if (nameLen > 0) System.arraycopy(nameBytes, 0, out, off, nameLen);

            encoded[idx++] = out;
            total += out.length;
        }

        byte[] finalOut = new byte[total];
        int off = 0;
        ByteUtils.addInt(finalOut, entities.size(), off);
        off += Integer.BYTES;
        for (byte[] b : encoded) {
            System.arraycopy(b, 0, finalOut, off, b.length);
            off += b.length;
        }

        Path parent = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION);
        Files.createDirectories(parent);
        Path file = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION + position3D.getPath().replace(".chunk", ENTITY_EXT));
        Files.write(file, finalOut);
    }

    public static Set<Entity> readEntities(Position3D position3D) throws IOException {
        byte[] bytes = get(position3D);
        if (bytes == null) return null;
        // Reading back into Entity objects is out of scope for now; return empty set to indicate presence
        return new HashSet<>();
    }
}