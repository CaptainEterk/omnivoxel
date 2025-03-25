package omnivoxel.server.client.chunk.biomeService.climate;

import java.util.Arrays;

public class ClimateVector {
    protected final double[] pos;

    public ClimateVector(double... pos) {
        this.pos = pos;
    }

    public double get(int i) {
        return pos[i];
    }

    public int size() {
        return pos.length;
    }

    public double getDistance(ClimateVector other) {
        if (other.pos.length != this.pos.length) {
            throw new IllegalArgumentException("Climate vectors must have equal sizes to calculate the distance");
        }

        double sum = 0;
        double[] otherPos = other.pos; // Local reference for fast access

        for (int i = 0; i < pos.length; i++) {
            double diff = this.pos[i] - otherPos[i];
            sum += diff * diff;
        }

        return sum;
    }

    @Override
    public String toString() {
        return Arrays.toString(pos);
    }
}