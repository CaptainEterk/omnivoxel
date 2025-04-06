package omnivoxel.server.client.structure;

import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.BlockService;
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

    abstract public @NotNull Structure initBlocks(BlockService blockService);

    abstract public @NotNull Map<Position3D, ServerBlock> getBlocks();
}