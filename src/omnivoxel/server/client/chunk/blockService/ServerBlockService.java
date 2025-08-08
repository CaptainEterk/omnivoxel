package omnivoxel.server.client.chunk.blockService;

import omnivoxel.server.client.block.ServerBlock;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerBlockService {
    private final Map<String, ServerBlock> serverBlocksById;

    public ServerBlockService() {
        serverBlocksById = new ConcurrentHashMap<>();
    }

    public ServerBlock getBlock(String id, int... blockState) {
        String key = createKey(id, blockState);
        ServerBlock serverBlock = serverBlocksById.get(key);
        if (serverBlock == null) {
            serverBlock = new ServerBlock(id, blockState);
            serverBlocksById.put(key, serverBlock);
        }

        return serverBlock;
    }

    private String createKey(String id, int[] blockState) {
        return id + ":" + Arrays.hashCode(blockState);
    }
}