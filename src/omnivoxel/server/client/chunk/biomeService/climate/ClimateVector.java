package omnivoxel.server.client.chunk.biomeService.climate;

import java.util.Arrays;
import java.util.stream.IntStream;

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

    public double getDistance(ClimateVector pos) {
        if (pos.size() != size()) {
            throw new IllegalArgumentException("The climate vectors must have equal sizes to calculate the distance");
        }
        return IntStream.range(0, pos.size())
                .mapToDouble(i -> Math.pow(this.pos[i] - pos.get(i), 2))
                .sum();
    }

    @Override
    public String toString() {
        return Arrays.toString(pos);
    }
}