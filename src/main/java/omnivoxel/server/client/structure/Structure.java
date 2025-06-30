package omnivoxel.server.client.structure;

import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.util.math.Position3D;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class Structure {
    protected final @NotNull StructureBoundingBox boundingBox;
    protected final @NotNull Position3D origin;

    protected Structure(int width, int height, int length, @NotNull Position3D origin) {
        this.boundingBox = StructureBoundingBox.create(width, height, length);
        this.origin = origin;
    }

    public final @NotNull StructureBoundingBox getBoundingBox() {
        return boundingBox;
    }

    public final @NotNull Position3D getOrigin() {
        return origin;
    }

    abstract public @NotNull Structure initBlocks(ServerBlockService blockService);

    abstract public @NotNull Map<Position3D, PriorityServerBlock> getBlocks();
}