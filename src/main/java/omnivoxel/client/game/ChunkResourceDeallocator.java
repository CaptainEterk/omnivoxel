package omnivoxel.client.game;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.math.Position3D;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChunkResourceDeallocator implements Runnable {
    private final GameState state;
    private final AtomicBoolean gameRunning;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final ClientWorld world;
    private final AtomicBoolean recalculate;
    private final Camera camera;
    private final Settings settings;
    private final List<Position3D> chunks = new CopyOnWriteArrayList<>();

    public ChunkResourceDeallocator(GameState state, AtomicBoolean gameRunning, BlockingQueue<Consumer<Long>> contextTasks, ClientWorld world, AtomicBoolean recalculate, Camera camera, Settings settings) {
        this.state = state;
        this.gameRunning = gameRunning;
        this.contextTasks = contextTasks;
        this.world = world;
        this.recalculate = recalculate;
        this.camera = camera;
        this.settings = settings;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        while (gameRunning.get()) {
            try {
                while (!recalculate.get() && System.currentTimeMillis() - time < ConstantGameSettings.AUTO_RECALCULATE_CHUNKS_TIME) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            recalculate.set(false);
//
//            chunks.clear();
//
//            world.getNewChunks().clear();
//
//            contextTasks.add(_ -> world.freeAllChunksNotIn(chunks));
        }
    }

    public List<Position3D> getChunks() {
        return chunks;
    }
}