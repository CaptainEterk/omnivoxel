package omnivoxel.server.client.chunk.worldDataService.density;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Function {
    String id();
}