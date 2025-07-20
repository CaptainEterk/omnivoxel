package omnivoxel.client.launcher;

import io.netty.util.ResourceLeakDetector;
import omnivoxel.client.game.GameLoop;
import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.camera.Frustum;
import omnivoxel.client.game.graphics.opengl.mesh.block.AirBlock;
import omnivoxel.client.game.graphics.opengl.text.TextRenderer;
import omnivoxel.client.game.player.PlayerController;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.State;
import omnivoxel.client.game.tick.TickLoop;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.ClientLauncher;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.util.log.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Launcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        OmniVoxel.init();

        SecureRandom secureRandom = new SecureRandom();
        byte[] clientID = new byte[32];
        secureRandom.nextBytes(clientID);

        CountDownLatch connected = new CountDownLatch(1);

        ClientWorldDataService clientWorldDataService = new ClientWorldDataService();

        clientWorldDataService.addBlock(new AirBlock());

        State state = new State();
        Settings settings = new Settings();
        settings.load();

        ClientWorld world = new ClientWorld(state);

        Client client = new Client(clientID, clientWorldDataService, new Logger("client"), world);
        ClientLauncher clientLauncher = new ClientLauncher(connected, client);
        Thread clientThread = new Thread(clientLauncher, "Client");
        clientThread.start();

        world.setClient(client);
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        if (connected.await(5L, TimeUnit.SECONDS)) {
            client.setListeners(world.getEntityMeshDefinitionCache(), world.getQueuedEntityMeshData());
            AtomicBoolean gameRunning = new AtomicBoolean(true);
            BlockingQueue<Consumer<Long>> contextTasks = new LinkedBlockingDeque<>();

            PlayerController playerController = new PlayerController(client, new Camera(new Frustum(), state), settings, contextTasks, state, world);

            GameLoop gameLoop = new GameLoop(playerController.getCamera(), world, gameRunning, contextTasks, client, state, settings, new TextRenderer());

            Thread tickLoopThread = new Thread(new TickLoop(playerController, gameRunning, contextTasks, client), "Tick Loop");
            tickLoopThread.start();

            gameLoop.run();
        }
    }
}