package omnivoxel.world.block;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockService {
    private final Map<String, Block> serverBlocksById;

    public BlockService() {
        serverBlocksById = new ConcurrentHashMap<>();
    }

    public Block getBlock(String id, int... blockState) {
        String key = createKey(id, blockState);
        Block block = serverBlocksById.get(key);
        if (block == null) {
            block = new Block(id, blockState);
            serverBlocksById.put(key, block);
        }

        return block;
    }

    private String createKey(String id, int[] blockState) {
        return id + ":" + Arrays.hashCode(blockState);
    }
}