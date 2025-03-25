package omnivoxel.client.launcher;

import core.blocks.*;
import omnivoxel.client.game.GameLoop;
import omnivoxel.client.game.player.PlayerController;
import omnivoxel.client.game.player.camera.Camera;
import omnivoxel.client.game.player.camera.Frustum;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.text.TextRenderer;
import omnivoxel.client.game.thread.mesh.block.AirBlock;
import omnivoxel.client.game.thread.mesh.shape.BlockShape;
import omnivoxel.client.game.thread.mesh.shape.ShallowBlockShape;
import omnivoxel.client.game.thread.tick.TickLoop;
import omnivoxel.client.game.world.World;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.ClientLauncher;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.debug.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Launcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] clientID = new byte[32];
        secureRandom.nextBytes(clientID);

        CountDownLatch connected = new CountDownLatch(1);

        Random random = new Random(0);

        ClientWorldDataService clientWorldDataService = new ClientWorldDataService();

        // Air
        clientWorldDataService.addBlock("omnivoxel", new AirBlock());

        // TODO: Add these with a mod loader
        // Regular blocks
        clientWorldDataService.addBlock("core", new StoneBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new DirtBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new GrassBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new WaterSourceBlock(new ShallowBlockShape(2), new BlockShape()));
        clientWorldDataService.addBlock("core", new SandBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new SnowBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new GlassBlock(new ShallowBlockShape(1)));

        // ORES
        clientWorldDataService.addBlock("core", new IronBlock(new BlockShape()));

        // DEBUG
        clientWorldDataService.addBlock("core", new RedBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new YellowBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new GreenBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new BlueBlock(new BlockShape()));
        clientWorldDataService.addBlock("core", new ClimateBlock(new BlockShape()));

        Client client = new Client(clientID, clientWorldDataService, new Logger());
        ClientLauncher clientLauncher = new ClientLauncher(connected, client);
        Thread clientThread = new Thread(clientLauncher, "Client");
        clientThread.start();

        if (connected.await(5L, TimeUnit.SECONDS)) {
            Settings settings = new Settings();
            settings.load();

            AtomicBoolean gameRunning = new AtomicBoolean(true);
            BlockingQueue<Consumer<Long>> contextTasks = new LinkedBlockingQueue<>();

            GameState gameState = new GameState();

            World world = new World(client, gameState);
            PlayerController playerController = new PlayerController(client, new Camera(new Frustum(), gameState), settings, contextTasks, gameState);

            GameLoop gameLoop = new GameLoop(playerController.getCamera(), world, gameRunning, contextTasks, client, gameState, settings, new TextRenderer());

            Thread tickLoopThread = new Thread(new TickLoop(playerController, gameRunning, contextTasks, client), "Tick Loop");
            tickLoopThread.start();

            gameLoop.run();
        }
    }
}