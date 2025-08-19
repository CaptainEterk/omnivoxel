package omnivoxel.server.client.chunk.blockService;

import omnivoxel.common.BlockShape;
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
            if (blockState != null) {
                ServerBlock noStateBlock = serverBlocksById.get(createKey(id, null));
                serverBlock = new ServerBlock(id, blockState, noStateBlock == null ? BlockShape.DEFAULT_BLOCK_SHAPE_STRING : noStateBlock.blockShape(), noStateBlock != null && noStateBlock.transparent());
            } else {
                serverBlock = new ServerBlock(id, null, BlockShape.DEFAULT_BLOCK_SHAPE_STRING, false);
            }
            serverBlocksById.put(key, serverBlock);
        }

        return serverBlock;
    }

    public Map<String, ServerBlock> getAllBlocks() {
        return serverBlocksById;
    }

    private String createKey(String id, int[] blockState) {
        return id + ":" + Arrays.hashCode(blockState);
    }

    public void addServerBlock(String id, ServerBlock serverBlock) {
        serverBlocksById.put(id, serverBlock);
    }
}