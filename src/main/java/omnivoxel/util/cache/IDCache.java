package omnivoxel.util.cache;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IDCache<V> {
    private final Map<String, V> cache = new ConcurrentHashMap<>();

    public void add(String id, V value) {
        cache.put(id, value);
    }

    public V get(String id, Class<? extends V> clazz) {
        return cache.computeIfAbsent(id, k -> {
            try {
                return clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
            }
        });
    }

    public V get(String id, Class<? extends V> clazz, Class<?>[] parameterTypes, Object[] args) {
        return cache.computeIfAbsent(id, k -> {
            try {
                Constructor<? extends V> constructor = clazz.getConstructor(parameterTypes);
                return constructor.newInstance(args);
            } catch (NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate " + clazz.getName() +
                        " with arguments", e);
            }
        });
    }
}