package omnivoxel.client.game.thread.tick;

import omnivoxel.client.game.player.PlayerController;
import omnivoxel.client.game.thread.mesh.util.PriorityUtils;
import omnivoxel.client.game.util.input.OVKeyInput;
import omnivoxel.client.game.util.input.OVMouseButtonInput;
import omnivoxel.client.game.util.input.OVMouseInput;
import omnivoxel.client.network.Client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TickLoop implements Runnable {
    private final PlayerController playerController;
    private final AtomicBoolean gameRunning;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final Client client;

    public TickLoop(PlayerController playerController, AtomicBoolean gameRunning, BlockingQueue<Consumer<Long>> contextTasks, Client client) {
        this.playerController = playerController;
        this.gameRunning = gameRunning;
        this.contextTasks = contextTasks;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            OVKeyInput ovKeyInput = new OVKeyInput();
            OVMouseButtonInput ovMouseButtonInput = new OVMouseButtonInput();
            OVMouseInput ovMouseInput = new OVMouseInput();

            contextTasks.add(ovKeyInput::init);
            contextTasks.add(ovMouseButtonInput::init);
            contextTasks.add(ovMouseInput::init);

            playerController.setKeyInput(ovKeyInput);
            playerController.setMouseButtonInput(ovMouseButtonInput);
            playerController.setMouseInput(ovMouseInput);

            while (gameRunning.get()) {
                // TODO: Tick all entities here
                playerController.tick(1 / 60f);
                client.getPlayers().values().forEach(playerEntity -> playerEntity.tick(1 / 60f));
                PriorityUtils.setCamera(playerController.getCamera());
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            gameRunning.set(false);
            throw e;
        }
    }
}