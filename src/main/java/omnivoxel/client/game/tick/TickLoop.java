package omnivoxel.client.game.tick;

import omnivoxel.client.game.player.PlayerController;
import omnivoxel.client.game.graphics.opengl.mesh.util.PriorityUtils;
import omnivoxel.client.game.graphics.opengl.input.KeyInput;
import omnivoxel.client.game.graphics.opengl.input.MouseButtonInput;
import omnivoxel.client.game.graphics.opengl.input.MouseInput;
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


    // TODO: Move this to the server. The client should update the PlayerController, but the server should be in charge of handling entities.
    @Override
    public void run() {
        try {
            KeyInput keyInput = new KeyInput();
            MouseButtonInput mouseButtonInput = new MouseButtonInput();
            MouseInput mouseInput = new MouseInput();

            contextTasks.add(keyInput::init);
            contextTasks.add(mouseButtonInput::init);
            contextTasks.add(mouseInput::init);

            playerController.setKeyInput(keyInput);
            playerController.setMouseButtonInput(mouseButtonInput);
            playerController.setMouseInput(mouseInput);

            while (gameRunning.get()) {
                // TODO: Tick all entities here
                playerController.tick(1 / 60f);
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