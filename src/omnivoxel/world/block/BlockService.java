package omnivoxel.world.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockService {
    private final Map<String, Block> serverBlocksById;

    public BlockService() {
        serverBlocksById = new ConcurrentHashMap<>();
    }

    public Block getBlock(String id) {
        Block block = serverBlocksById.get(id);
        if (block == null) {
            block = new Block(id);
            serverBlocksById.put(id, block);
        }

        return block;
    }
}