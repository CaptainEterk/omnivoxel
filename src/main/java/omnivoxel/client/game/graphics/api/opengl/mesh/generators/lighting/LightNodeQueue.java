package omnivoxel.client.game.graphics.api.opengl.mesh.generators.lighting;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;

public final class LightNodeQueue {
    private static final int DEFAULT_CAPACITY = ConstantCommonSettings.BLOCKS_IN_CHUNK;

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
        if (allSameLightCount > 0) {
            throw new IllegalStateException("Cannot add to queue when all the light levels are the same");
        }
        xs[tail] = x;
        ys[tail] = y;
        zs[tail] = z;
        lightLevels[tail] = lightLevel;
        tail++;
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

    public void fill(byte lightEmitting) {
        allSameLightCount = ConstantCommonSettings.BLOCKS_IN_CHUNK - 1;
        lightLevel = lightEmitting;
    }
}