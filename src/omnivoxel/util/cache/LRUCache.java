package omnivoxel.util.cache;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

public class LRUCache<K, V> {
    private final ConcurrentHashMap<K, Node<K, V>> map = new ConcurrentHashMap<>();
    private final Deque<Node<K, V>> deque = new ArrayDeque<>();
    private final ReentrantLock dequeLock = new ReentrantLock();

    private final int limit;

    public LRUCache(int limit) {
        this.limit = limit;
    }

    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) return null;

        // Move to front of deque
        dequeLock.lock();
        try {
            deque.remove(node);
            deque.addFirst(node);
        } finally {
            dequeLock.unlock();
        }
        return node.value;
    }

    public void put(K key, V value) {
        Node<K, V> newNode = new Node<>(key, value);
        Node<K, V> old = map.put(key, newNode);

        dequeLock.lock();
        try {
            if (old != null) deque.remove(old);
            deque.addFirst(newNode);

            while (map.size() > limit) {
                Node<K, V> tail = deque.removeLast();
                map.remove(tail.key);
            }
        } finally {
            dequeLock.unlock();
        }
    }

    private static class Node<K, V> {
        final K key;
        final V value;
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}