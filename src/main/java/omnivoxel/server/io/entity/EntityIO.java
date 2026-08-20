package omnivoxel.server.io.entity;

import omnivoxel.common.settings.ConstantServerSettings;
import omnivoxel.server.entity.Entity;
import omnivoxel.server.entity.mob.PlayerEntity;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class EntityIO {
    private static final Function<byte[], Entity>[] entityDecodeFunctions;

    static {
        // TODO: This depends on EntityType order being the same as entityDecodeFunctions - fix this
        entityDecodeFunctions = new Function[1];
        entityDecodeFunctions[0] = PlayerEntity::decode;
    }

    private static Path getPath(long id) {
        return Path.of(ConstantServerSettings.ENTITY_SAVE_LOCATION, id + ".entity");
    }

    public static void writeEntity(long id, Entity entity) {
        byte[] entityBytes = entity.getBytes();
        Path path = getPath(id);
        try {
            Files.write(path, entityBytes);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

    public static Entity decode(byte[] bytes) {
        int entityID = ByteUtils.getInt(bytes, 0);

        return entityDecodeFunctions[entityID].apply(bytes);
    }

    public static Entity decode(int entityID, byte[] bytes) {
        return entityDecodeFunctions[entityID].apply(bytes);
    }

    public static Entity getEntity(long id) {
        try {
            Path path = getPath(id);

            if (!Files.exists(path)) {
                Logger.warn("Entity " + id + " does not exist.");
                return null;
            }

            byte[] content = Files.readAllBytes(path);

            if (content.length < 4) {
                Logger.warn("Entity " + id + " is invalid.");
                return null;
            }

            return decode(content);
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    public static byte[] encode(Set<Long> entities) {
        byte[] out = new byte[Integer.BYTES + Long.BYTES * entities.size()];
        ByteUtils.addInt(out, entities.size(), 0);
        int i = Integer.BYTES;
        for (Long entityId : entities) {
            ByteUtils.addLong(out, entityId, i);
            i += Long.BYTES;
        }
        return out;
    }

    public static boolean exists(long id) {
        Path path = getPath(id);

        return Files.exists(path);
    }
}