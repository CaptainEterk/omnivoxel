package omnivoxel.server.entity;

import omnivoxel.common.settings.ConstantServerSettings;
import omnivoxel.server.io.entity.EntityIO;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.util.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public final class EntityStorageManager {
    private final EntityStorage entityStorage;
    private final SecureRandom random = new SecureRandom();

    public EntityStorageManager(EntityStorage entityStorage) {
        this.entityStorage = entityStorage;
    }

    public Entity getEntity(long id) {
        Entity entity = entityStorage.get(id);
        if (entity != null) return entity;

        return EntityIO.getEntity(id);
    }

    public long translateClientToEntity(String clientID) {
        try {
            Path path = Path.of(ConstantServerSettings.PLAYER_SAVE_LOCATION, clientID);

            if (!Files.exists(path)) {
                return -1L;
            }

            byte[] bytes = Files.readAllBytes(path);

            if (bytes.length != Long.BYTES) {
                Logger.error("Invalid entity ID file for client " + clientID);
                return -1L;
            }

            return ByteUtils.getLong(bytes, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeClientToEntityTranslation(String clientID, long entityID) {
        try {
            Path path = Path.of(ConstantServerSettings.PLAYER_SAVE_LOCATION, clientID);

            Files.createDirectories(path.getParent());

            byte[] bytes = new byte[Long.BYTES];
            ByteUtils.addLong(bytes, entityID, 0);

            Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write client translation", e);
        }
    }

    public long getNewEntityID() {
        long id = random.nextLong();
        while (entityStorage.get(id) != null || idExists(id)) {
            id = random.nextLong();
        }
        return id;
    }

    private boolean idExists(long id) {
        return EntityIO.exists(id);
    }
}