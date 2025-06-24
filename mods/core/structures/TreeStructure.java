package core.structures;

import omnivoxel.math.Position3D;
import omnivoxel.server.client.block.PriorityServerBlock;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TreeStructure extends Structure {
    private final Map<Position3D, PriorityServerBlock> blocks = new HashMap<>();

    public TreeStructure() {
        super(3, 5, 3, new Position3D(0, 0, 0));
    }

    @Override
    public @NotNull Structure initBlocks(BlockService blockService) {
        for (int y = 0; y < 5; y++) {
            blocks.put(new Position3D(0, y, 0), new PriorityServerBlock(blockService.getBlock("core:log_block", null), PriorityServerBlock.Priority.STRUCTURE_PRIMARY));
        }
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                if (!((x == 0 || x == 4) && (z == 0 || z == 4)) && !(x == 2 && z == 2)) {
                    for (int y = 0; y < 1; y++) {
                        blocks.put(new Position3D(x - 2, y + 3, z - 2), new PriorityServerBlock(blockService.getBlock("core:leaf_block", null), PriorityServerBlock.Priority.DECORATION));
                    }
                }
            }
        }
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                blocks.put(new Position3D(x - 1, 4, z - 1), new PriorityServerBlock(blockService.getBlock("core:leaf_block", null), PriorityServerBlock.Priority.DECORATION));
            }
        }
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (!((x == 0 || x == 2) && (z == 0 || z == 2))) {
                    blocks.put(new Position3D(x - 1, 5, z - 1), new PriorityServerBlock(blockService.getBlock("core:leaf_block", null), PriorityServerBlock.Priority.DECORATION));
                }
            }
        }
        return this;
    }

    @Override
    public @NotNull Map<Position3D, PriorityServerBlock> getBlocks() {
        return blocks;
    }
}