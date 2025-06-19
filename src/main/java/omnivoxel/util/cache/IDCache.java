package omnivoxel.util.cache;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IDCache<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public void add(K key, V value) {
        cache.put(key, value);
    }

    public V get(K key, Class<? extends V> clazz) {
        return cache.computeIfAbsent(key, k -> {
            if (clazz == null) {
                return null;
            }
            try {
                return clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
            }
        });
    }

    public V get(K key, Class<? extends V> clazz, Class<?>[] parameterTypes, Object[] args) {
        return cache.computeIfAbsent(key, k -> {
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

    public void put(K key, V value) {
        cache.put(key, value);
    }
}