package omnivoxel.server.client.chunk.biomeService.climate;

import java.util.Arrays;

public class ClimateVector {
    protected final double[] pos;

    public ClimateVector(double... pos) {
        this.pos = pos;
    }

    public ClimateVector(ClimateVector climateVector) {
        this.pos = new double[climateVector.size()];
        for (int i = 0; i < this.pos.length; i++) {
            this.pos[i] = climateVector.get(i);
        }
    }

    public double get(int i) {
        return pos[i];
    }

    public int size() {
        return pos.length;
    }

    public final double getDistance(ClimateVector other) {
        double[] a = this.pos;
        double[] b = other.pos;

        if (a.length != b.length)
            throw new IllegalArgumentException("Vector dimensions must match.");

        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return sum;
    }


    @Override
    public String toString() {
        return Arrays.toString(pos);
    }

    public double[] getArray() {
        return pos;
    }

    public ClimateVector copyAndAdd(ClimateVector climateVector, int... indexes) {
        double[] values = new double[indexes.length + climateVector.size()];
        for (int i = 0; i < indexes.length; i++) {
            values[i] = pos[indexes[i]];
        }
        for (int i = 0; i < climateVector.size(); i++) {
            values[i + indexes.length] = climateVector.get(i);
        }
        return new ClimateVector(values);
    }
}