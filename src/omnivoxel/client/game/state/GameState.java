package omnivoxel.client.game.state;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private final Map<String, Object> state = new HashMap<>();

    public <T> void setItem(String key, T value) {
        state.put(key, value);
    }

    public <T> T getItem(String key, Class<T> valueType) {
        Object value = state.get(key);
        if (valueType.isInstance(value)) {
            return valueType.cast(value);
        }
        throw new IllegalArgumentException("Invalid type for key: " + key);
    }
}