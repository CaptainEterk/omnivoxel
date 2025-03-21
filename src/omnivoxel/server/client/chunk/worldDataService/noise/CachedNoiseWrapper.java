package omnivoxel.server.client.chunk.worldDataService.noise;

import java.util.HashMap;
import java.util.Map;

public class CachedNoiseWrapper implements Noise2D {
    private final Noise2D wrappedNoise;
    private final Map<Position2D, Double> cachedValues;

    public CachedNoiseWrapper(Noise2D wrappedNoise) {
        this.wrappedNoise = wrappedNoise;
        cachedValues = new HashMap<>();
    }

    @Override
    public double generate(double x, double z) {
//        Position2D position2D = new Position2D(x, z);
//        if (cachedValues.containsKey(position2D)) {
//            return cachedValues.get(position2D);
//        }
//        double value = wrappedNoise.generate(x, z);
//        // Cache value
//        cachedValues.put(new Position2D(x, z), value);
//        return value;
        return wrappedNoise.generate(x, z);
    }

    private record Position2D(double x, double z) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Position2D that = (Position2D) o;
            return Double.compare(x(), that.x()) == 0 && Double.compare(z(), that.z()) == 0;
        }
    }
}