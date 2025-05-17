package core.structures;

import omnivoxel.math.Position3D;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TestStructure extends Structure {
    private final Map<Position3D, ServerBlock> blocks = new HashMap<>();

    public TestStructure() {
        super(10, 10, 10, new Position3D(0, 0, 0));
    }

    @Override
    public @NotNull Structure initBlocks(BlockService blockService) {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    blocks.put(new Position3D(x, y, z), new ServerBlock("core:iron_block"));
                }
            }
        }
        return this;
    }

    @Override
    public @NotNull Map<Position3D, ServerBlock> getBlocks() {
        return blocks;
    }
}