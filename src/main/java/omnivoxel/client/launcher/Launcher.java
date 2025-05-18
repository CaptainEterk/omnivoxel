package omnivoxel.client.launcher;

import core.blocks.*;
import omnivoxel.client.game.GameLoop;
import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.camera.Frustum;
import omnivoxel.client.game.player.PlayerController;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.text.TextRenderer;
import omnivoxel.client.game.thread.mesh.block.AirBlock;
import omnivoxel.client.game.thread.mesh.shape.BlockShape;
import omnivoxel.client.game.thread.mesh.shape.ShallowBlockShape;
import omnivoxel.client.game.thread.tick.TickLoop;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.ClientLauncher;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.util.Logger;

import java.io.IOException;
import java.security.SecureRandom;
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

        ClientWorldDataService clientWorldDataService = new ClientWorldDataService();

        // Air
        clientWorldDataService.addBlock(new AirBlock());

        // TODO: Add these with a mod loader
        // Regular blocks
        clientWorldDataService.addBlock(new StoneBlock(new BlockShape()));
        clientWorldDataService.addBlock(new DirtBlock(new BlockShape()));
        clientWorldDataService.addBlock(new GrassBlock(new BlockShape()));
        clientWorldDataService.addBlock(new WaterSourceBlock(new ShallowBlockShape(2), new BlockShape()));
        clientWorldDataService.addBlock(new SandBlock(new BlockShape()));
        clientWorldDataService.addBlock(new SnowBlock(new BlockShape()));
        clientWorldDataService.addBlock(new GlassBlock(new BlockShape()));
        clientWorldDataService.addBlock(new IceBlock(new BlockShape()));

        // ORES
        clientWorldDataService.addBlock(new IronBlock(new BlockShape()));

        // DEBUG
        clientWorldDataService.addBlock(new RedBlock(new BlockShape()));
        clientWorldDataService.addBlock(new YellowBlock(new BlockShape()));
        clientWorldDataService.addBlock(new GreenBlock(new BlockShape()));
        clientWorldDataService.addBlock(new BlueBlock(new BlockShape()));
        clientWorldDataService.addBlock(new ClimateBlock(new BlockShape()));

        GameState gameState = new GameState();

        ClientWorld world = new ClientWorld(gameState);

        Client client = new Client(clientID, clientWorldDataService, new Logger());
        ClientLauncher clientLauncher = new ClientLauncher(connected, client);
        Thread clientThread = new Thread(clientLauncher, "Client");
        clientThread.start();

        client.setChunkListener(world::add);
        world.setClient(client);

        if (connected.await(5L, TimeUnit.SECONDS)) {
            Settings settings = new Settings();
            settings.load();

            AtomicBoolean gameRunning = new AtomicBoolean(true);
            BlockingQueue<Consumer<Long>> contextTasks = new LinkedBlockingQueue<>();

            PlayerController playerController = new PlayerController(client, new Camera(new Frustum(), gameState), settings, contextTasks, gameState);

            GameLoop gameLoop = new GameLoop(playerController.getCamera(), world, gameRunning, contextTasks, client, gameState, settings, new TextRenderer());

            Thread tickLoopThread = new Thread(new TickLoop(playerController, gameRunning, contextTasks, client), "Tick Loop");
            tickLoopThread.start();

            gameLoop.run();
        }
    }
}