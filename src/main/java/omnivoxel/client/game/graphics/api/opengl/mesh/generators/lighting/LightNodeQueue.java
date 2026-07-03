package omnivoxel.client.game.graphics.api.opengl.mesh.generators.lighting;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;

import java.util.Arrays;

public final class LightNodeQueue {
        private static final int DEFAULT_CAPACITY = 256;

        private int[] xs = new int[DEFAULT_CAPACITY];
        private int[] ys = new int[DEFAULT_CAPACITY];
        private int[] zs = new int[DEFAULT_CAPACITY];
        private byte[] lightLevels = new byte[DEFAULT_CAPACITY];
        private int head;
        private int tail;
        private int x;
        private int y;
        private int z;
        private byte lightLevel;
        private int allSameLightCount;

        public void add(int x, int y, int z, byte lightLevel) {
            ensureCapacity();
            xs[tail] = x;
            ys[tail] = y;
            zs[tail] = z;
            lightLevels[tail] = lightLevel;
            tail++;
            if (allSameLightCount > 0) {
                throw new IllegalStateException("Cannot add to queue when all the light levels are the same");
            }
        }

        public void poll() {
            if (allSameLightCount > 0) {
                x = IndexCalculator.x(allSameLightCount);
                y = IndexCalculator.y(allSameLightCount);
                z = IndexCalculator.z(allSameLightCount);
                allSameLightCount--;
            } else {
                x = xs[head];
                y = ys[head];
                z = zs[head];
                lightLevel = lightLevels[head];
                head++;
            }
        }

        public boolean isEmpty() {
            return head == tail && allSameLightCount == 0;
        }

        public void clear() {
            head = 0;
            tail = 0;
            allSameLightCount = 0;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        public byte lightLevel() {
            return lightLevel;
        }

        private void ensureCapacity() {
            if (tail < xs.length) {
                return;
            }

            if (head > 0) {
                int size = tail - head;
                System.arraycopy(xs, head, xs, 0, size);
                System.arraycopy(ys, head, ys, 0, size);
                System.arraycopy(zs, head, zs, 0, size);
                System.arraycopy(lightLevels, head, lightLevels, 0, size);
                head = 0;
                tail = size;
                return;
            }

            int newCapacity = xs.length << 1;
            xs = Arrays.copyOf(xs, newCapacity);
            ys = Arrays.copyOf(ys, newCapacity);
            zs = Arrays.copyOf(zs, newCapacity);
            lightLevels = Arrays.copyOf(lightLevels, newCapacity);
        }

        public void fill(byte lightEmitting) {
            allSameLightCount = ConstantCommonSettings.BLOCKS_IN_CHUNK;
            lightLevel = lightEmitting;
        }
    }