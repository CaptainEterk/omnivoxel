package omnivoxel.client.network.chunk.worldDataService;

import omnivoxel.client.game.graphics.opengl.mesh.block.Block;
import omnivoxel.client.game.graphics.opengl.mesh.block.BlockStateWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientWorldDataService {
    private final Map<String, BlockMap> blocks;

    public ClientWorldDataService() {
        blocks = new ConcurrentHashMap<>();
    }

    public Block getBlock(String blockModID, String state) {
        return blocks.get(blockModID).get(state);
    }

    public void addBlock(Block block) {
        blocks.put(block.getModID(), new BlockMap(new ConcurrentHashMap<>(), block));
    }

    private record BlockMap(Map<String, Block> blocks, Block block) {
        public Block get(String state) {
            if (state == null) {
                return block;
            }
            Block b = blocks.get(state);
            if (b == null) {
                b = new BlockStateWrapper(block, state);
                blocks.put(state, b);
            }
            return b;
        }
    }
}