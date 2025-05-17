package omnivoxel.client.network.chunk.worldDataService;

import omnivoxel.client.game.thread.mesh.block.Block;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClientWorldDataService {
    private final Map<StateIDPair, Block> blocks;

    public ClientWorldDataService() {
        blocks = new ConcurrentHashMap<>();
    }

    public Block getBlock(String blockModID, int[] state) {
        for (Map.Entry<StateIDPair, Block> entry : blocks.entrySet()) {
            StateIDPair stateIDPair = entry.getKey();
            if (Objects.equals(stateIDPair.id(), blockModID)) {
                if (stateIDPair.state == state) {
                    return entry.getValue();
                }
                if (stateIDPair.state().length == state.length) {
                    boolean same = true;
                    for (int i = 0; i < state.length; i++) {
                        if (stateIDPair.state[i] != state[i]) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    public void addBlock(Block block) {
        blocks.put(new StateIDPair(block.getModID(), block.getState()), block);
    }

    private record StateIDPair(String id, int[] state) {

    }
}