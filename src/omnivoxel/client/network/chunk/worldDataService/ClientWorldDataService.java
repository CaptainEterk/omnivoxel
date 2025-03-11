package omnivoxel.client.network.chunk.worldDataService;

import omnivoxel.client.game.thread.mesh.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ClientWorldDataService {
    private final Map<String, Block> blocks;

    public ClientWorldDataService() {
        blocks = new HashMap<>();
    }

    public Block getBlock(String blockModID) {
        return blocks.get(blockModID);
    }

    public void addBlock(String modID, Block block) {
        blocks.put(modID + ":" + block.getID(), block);
    }
}