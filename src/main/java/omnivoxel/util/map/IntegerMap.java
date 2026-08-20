package omnivoxel.util.map;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class IntegerMap {
        private static final int CAPACITY = 1024;

        private final int[] values = new int[CAPACITY];
        private final boolean[] used = new boolean[CAPACITY];
        private int size;

        public void reset() {
            if (size > 0) {
                Arrays.fill(used, false);
                size = 0;
            }
        }

        public void putLarger(int key, int value) {
            if (!used[key]) {
                used[key] = true;
                values[key] = value;
                size++;
                return;
            }

            if (value > values[key]) {
                values[key] = value;
            }
        }

        public int size() {
            return size;
        }

        public void forEach(BiConsumer<Integer, Integer> consumer) {
            for (int i = 0; i < CAPACITY; i++) {
                if (used[i]) {
                    consumer.accept(i, values[i]);
                }
            }
        }
    }