package omnivoxel.server.client.chunk.worldDataService.density;

import org.graalvm.polyglot.Value;

public abstract class DensityFunction {
    public DensityFunction(Value[] args, long i) {
        if (!this.getClass().isAnnotationPresent(Function.class)) {
            throw new IllegalStateException(
                    this.getClass().getName() + " must be annotated with @Function"
            );
        }
    }

    public abstract double evaluate(double x, double y, double z);
}