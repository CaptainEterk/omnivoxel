package omnivoxel.client.network.chunk.worldDataService;

import omnivoxel.client.game.thread.mesh.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientWorldDataService {
    private final Map<StateIDPair, Block> blocks;

    public ClientWorldDataService() {
        blocks = new ConcurrentHashMap<>();
    }

    public Block getBlock(String blockModID, int[] state) {
        return blocks.get(new StateIDPair(blockModID, state));
    }

    public void addBlock(Block block) {
        blocks.put(new StateIDPair(block.getModID(), block.getState()), block);
    }

    private record StateIDPair(String id, int[] state) {

    }
}