package omnivoxel.client.network.chunk.worldDataService;

import omnivoxel.client.game.thread.mesh.block.Block;
import omnivoxel.client.game.thread.mesh.block.BlockStateWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientWorldDataService {
    private final Map<String, BlockMap> blocks;

    public ClientWorldDataService() {
        blocks = new ConcurrentHashMap<>();
    }

    public Block getBlock(String blockModID, int[] state) {
        return blocks.get(blockModID).get(state);
    }

    public void addBlock(Block block) {
        blocks.put(block.getModID(), new BlockMap(new ConcurrentHashMap<>(), block));
    }

    private record StateIDPair(String id, int[] state) {

    }

    private record BlockMap(Map<int[], Block> blocks, Block block) {
        public Block get(int[] state) {
            if (state == null) {
                return block;
            }
            return blocks.computeIfAbsent(state, s -> new BlockStateWrapper(block, s));
        }
    }
}