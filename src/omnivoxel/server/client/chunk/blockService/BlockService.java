package omnivoxel.server.client.chunk.blockService;

import omnivoxel.server.client.block.ServerBlock;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockService {
    // A map where the key is a composite of id and blockState for faster lookup
    private final ConcurrentHashMap<String, ServerBlock> serverBlocksById;

    public BlockService() {
        serverBlocksById = new ConcurrentHashMap<>();
    }

    public ServerBlock getBlock(String id, int[] blockState) {
        // Create a unique key based on id and blockState
        String key = createKey(id, blockState);

        // Try to retrieve the block by key
        ServerBlock serverBlock = serverBlocksById.get(key);

        if (serverBlock == null) {
            // If not found, create a new block and add it to the map
            serverBlock = new ServerBlock(id, blockState);
            serverBlocksById.put(key, serverBlock);
        }

        return serverBlock;
    }

    private String createKey(String id, int[] blockState) {
        // Create a unique key based on id and the content of the blockState
        return id + ":" + Arrays.hashCode(blockState); // Using hashCode of blockState for uniqueness
    }
}