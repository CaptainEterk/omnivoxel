package omnivoxel.client.game.thread.mesh.util.cache;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private final Map<K, Node<K, V>> keyToNode;

    private int limit;
    private Node<K, V> head;
    private Node<K, V> tail;

    public LRUCache() {
        this(10);
    }

    public LRUCache(int limit) {
        keyToNode = new HashMap<>();
        this.limit = limit;
    }

    public synchronized void put(K key, V value) {
        Node<K, V> node = keyToNode.get(key);
        if (node == null) {
            Node<K, V> newNode = new Node<>(key, value);
            keyToNode.put(key, newNode);
            node = newNode;
        } else {
            node.setValue(value);
            removeNode(node);
        }

        makeNodeTheHead(node);

        while (keyToNode.size() > limit) {
            keyToNode.remove(tail.getKey());
            removeNode(tail);
        }
    }

    public synchronized V get(K key) {
        Node<K, V> node = keyToNode.get(key);
        if (node == null) {
            return null;
        } else {
            removeNode(node);
            makeNodeTheHead(node);
            return node.getValue();
        }
    }

    public synchronized void setLimit(int limit) {
        this.limit = limit;
    }

    private synchronized void removeNode(Node<K, V> node) {
        boolean head = node.getLeft() == null;
        boolean tail = node.getRight() == null;

        if (head && tail) {
            this.head = null;
            this.tail = null;
        } else if (head) {
            this.head = node.getRight();
            node.getRight().setLeft(null);
        } else if (tail) {
            this.tail = node.getLeft();
            node.getLeft().setRight(null);
        } else {
            node.getLeft().setRight(node.getRight());
            node.getRight().setLeft(node.getLeft());
        }
        node.setLinks(null, null);
    }

    private void makeNodeTheHead(Node<K, V> node) {
        if (head == null) {
            node.setLinks(null, null);
            head = node;
            tail = node;
        } else {
            node.setLinks(null, head);
            head.setLeft(node);
            head = node;
        }
    }

    static class Node<K, V> {
        private final K key;
        private Node<K, V> left;
        private Node<K, V> right;
        private V value;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node<K, V> getLeft() {
            return left;
        }

        public void setLeft(Node<K, V> left) {
            this.left = left;
        }

        public Node<K, V> getRight() {
            return right;
        }

        public void setRight(Node<K, V> right) {
            this.right = right;
        }

        public void setLinks(Node<K, V> left, Node<K, V> right) {
            setLeft(left);
            setRight(right);
        }

        @Override
        public String toString() {
            return "Node{key=" + key +
                    ", value=" + value +
                    '}';
        }
    }
}