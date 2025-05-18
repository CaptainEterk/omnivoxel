package omnivoxel.client.game;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.mesh.Mesh;
import omnivoxel.client.game.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.mesh.util.MeshGenerator;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.shader.ShaderProgram;
import omnivoxel.client.game.shader.ShaderProgramHandler;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.text.Alignment;
import omnivoxel.client.game.text.TextRenderer;
import omnivoxel.client.game.text.font.Font;
import omnivoxel.client.game.texture.TextureLoader;
import omnivoxel.client.game.window.Window;
import omnivoxel.client.game.window.WindowFactory;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.Client;
import omnivoxel.math.Position3D;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.util.Logger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class GameLoop {
    private static final int FPS_SAMPLES = 50;
    private final Camera camera;
    private final ClientWorld world;
    private final AtomicBoolean gameRunning;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final Client client;
    private final GameState gameState;
    private final Settings settings;
    private final TextRenderer textRenderer;
    private final double[] fpsHistory = new double[FPS_SAMPLES];
    private final long[] timeHistory = new long[FPS_SAMPLES];
    private ShaderProgram shaderProgram;
    private int fpsIndex = 0;
    private int timeIndex = 0;

    public GameLoop(Camera camera, ClientWorld world, AtomicBoolean gameRunning, BlockingQueue<Consumer<Long>> contextTasks, Client client, GameState gameState, Settings settings, TextRenderer textRenderer) {
        this.camera = camera;
        this.world = world;
        this.gameRunning = gameRunning;
        this.contextTasks = contextTasks;
        this.client = client;
        this.gameState = gameState;
        this.settings = settings;
        this.textRenderer = textRenderer;
    }

    public void run() {
        try {
            Window window = WindowFactory.createWindow(500, 500, ConstantGameSettings.DEFAULT_WINDOW_TITLE, new Logger());

            // Initializes the default shader
            // TODO: Make the player able to use their shaders instead.
            ShaderProgramHandler shaderProgramHandler = new ShaderProgramHandler();
            shaderProgramHandler.addShaderProgram("default", Map.of("assets/shaders/default.vert", GL20.GL_VERTEX_SHADER, "assets/shaders/default.frag", GL20.GL_FRAGMENT_SHADER));
            shaderProgramHandler.addShaderProgram("text", Map.of("assets/shaders/text.vert", GL20.GL_VERTEX_SHADER, "assets/shaders/text.frag", GL20.GL_FRAGMENT_SHADER));
            this.shaderProgram = shaderProgramHandler.getShaderProgram("default");
            ShaderProgram textShaderProgram = shaderProgramHandler.getShaderProgram("text");
            shaderProgram.bind();
            shaderProgram.setUniform("fogColor", 0.0f, 0.61568627451f, 1.0f, 1.0f);
            shaderProgram.setUniform("fogFar", settings.getFloatSetting("render_distance", 100));
            shaderProgram.setUniform("fogNear", settings.getFloatSetting("render_distance", 100) / 10 * 9);
            shaderProgram.unbind();

            textShaderProgram.bind();

            textShaderProgram.addUniformLocation("textColor");
            textShaderProgram.setUniform("textColor", 1f, 1f, 1f);

            textShaderProgram.addUniformLocation("projection");

            textShaderProgram.unbind();

            window.addMatrixListener(w -> textShaderProgram.setUniform("projection", new Matrix4f().ortho(0.0f, w.getWidth(), w.getHeight(), 0.0f, -1.0f, 1.0f)));

            gameState.setItem("shouldUpdateView", true);
            gameState.setItem("shouldUpdateVisibleMeshes", true);
            gameState.setItem("shouldCheckNewChunks", false);
            gameState.setItem("shouldRenderWireframe", false);
            gameState.setItem("seeDebug", true);
            gameState.setItem("bufferizingQueueSize", 0);

            gameState.setItem("inflight_requests", 0);
            gameState.setItem("chunk_requests_sent", 0);
            gameState.setItem("chunk_requests_received", 0);

            textRenderer.init();

            Font font = Font.create("Minecraft.ttf");

            MeshGenerator meshGenerator = new MeshGenerator();

            GL11C.glClearColor(0.0f, 0.61568627451f, 1.0f, 1.0f);

            // TODO: Make this stitch textures together and save texture coordinates in a string->(x, y) map.
            // TODO: Make the user be able to use texture packs instead (by loading it and stitching it together)
            int texture = TextureLoader.loadTexture("texture_atlas.png");

            // Enable depth testing for solid chunks
            GL11C.glDepthFunc(GL11C.GL_BACK);
            GL11C.glEnable(GL11C.GL_CULL_FACE);
            GL11C.glCullFace(GL11C.GL_BACK);

            List<Position3D> renderedChunks = new ArrayList<>();
            int totalChunks = 0;
            int totalRenderedChunks = 0;

            window.init(500, 500);
            window.show();

            double time = GLFW.glfwGetTime();

            String rightDebugText = "";
            double secondTime = time;

            boolean completeRenderDistance = false;

            AtomicBoolean recalculateRenderedChunks = new AtomicBoolean(true);

            ChunkResourceDeallocator chunkResourceDeallocators = new ChunkResourceDeallocator(gameState, gameRunning, contextTasks, world, recalculateRenderedChunks, camera, settings);
            Thread chunkResourceDeallocatorThread = new Thread(chunkResourceDeallocators);
            chunkResourceDeallocatorThread.start();

            // Renders everything
            while (!window.shouldClose()) {
                // Clear the framebuffer
                GL11.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

                // Bind the shader program
                shaderProgram.bind();

                // Bind the texture atlas
                GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);

                // RENDER

                // Render wireframe or not
                if (gameState.getItem("shouldRenderWireframe", Boolean.class)) {
                    GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_LINE);
                } else {
                    GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);
                }

                // Set camera position uniforms
                shaderProgram.setUniform("cameraPosition", -camera.getX(), -camera.getY(), -camera.getZ());

                // Render chunks
                shaderProgram.setUniform("useChunkPosition", true);
                shaderProgram.setUniform("useExactPosition", false);

                if (gameState.getItem("shouldUpdateView", Boolean.class)) {
                    Matrix4f projectionMatrix = new Matrix4f().setPerspective((float) Math.toRadians(camera.getFOV()), window.aspectRatio(), camera.getNear(), camera.getFar());
                    Matrix4f viewMatrix = new Matrix4f().identity().rotate(camera.getPitch(), 1, 0, 0).rotate(camera.getYaw(), 0, 1, 0).translate(camera.getX(), camera.getY(), camera.getZ());

                    camera.getFrustum().updateFrustum(projectionMatrix, viewMatrix);
                    shaderProgram.setUniform("projection", projectionMatrix);
                    shaderProgram.setUniform("view", viewMatrix);

                    gameState.setItem("shouldUpdateView", false);
                }

                if (gameState.getItem("shouldUpdateVisibleMeshes", Boolean.class)) {
//                    recalculateRenderedChunks.set(true);

                    renderedChunks.clear();
                    List<Position3D> chunks = calculateRenderedChunks(settings.getIntSetting("render_distance", 100));
                    totalRenderedChunks = chunks.size();
                    renderedChunks.addAll(chunks.stream().filter(chunkPosition -> world.get(chunkPosition) != null).toList());
                }

                if (world.totalQueuedChunks() < ConstantServerSettings.QUEUED_CHUNKS_MINIMUM && !completeRenderDistance) {
                    gameState.setItem("shouldUpdateVisibleMeshes", true);
                }

                if (gameState.getItem("shouldCheckNewChunks", Boolean.class)) {
                    Set<Position3D> newChunks = world.getNewChunks();
                    newChunks.removeIf(chunkPosition -> world.get(chunkPosition) == null);
                    newChunks.removeIf(chunkPosition -> !camera.getFrustum().isMeshInFrustum(chunkPosition));
                    renderedChunks.addAll(newChunks);
                    newChunks.clear();
                    gameState.setItem("shouldCheckNewChunks", false);
                }

                GL11C.glEnable(GL11C.GL_DEPTH_TEST);
                GL11C.glEnable(GL11C.GL_CULL_FACE);

                List<Position3D> renderedChunksInFrustum = renderedChunks.stream().filter(chunkPosition -> camera.getFrustum().isMeshInFrustum(chunkPosition)).toList();

                // Solid chunk meshes
                renderedChunksInFrustum.forEach(chunkPosition -> renderMesh(chunkPosition, world.get(chunkPosition).getMesh(), false));

                // Transparent Chunk Meshes
                GL11C.glDisable(GL11C.GL_CULL_FACE);
                GL11C.glDepthMask(false);
                GL11C.glEnable(GL11C.GL_BLEND);
                GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
                renderedChunksInFrustum.forEach(chunkPosition -> renderMesh(chunkPosition, world.get(chunkPosition).getMesh(), true));
                GL11C.glDisable(GL11C.GL_BLEND);
                GL11C.glDepthMask(true);

                // Render entities
//                shaderProgram.setUniform("useChunkPosition", false);
//                shaderProgram.setUniform("useExactPosition", true);
//                Map<String, EntityMesh> entityMeshes = world.getEntityMeshes();
//                for (Map.Entry<String, EntityMesh> entry : entityMeshes.entrySet()) {
//                    renderMesh(world.getEntity(entry.getKey()).getPosition(), entry.getValue(), false);
//                }

                // Bufferize chunks
                // TODO: Make the bufferizer actually use the endTime
                int bufferizedChunkCount = world.bufferizeChunks(meshGenerator, System.nanoTime());

                // Unbind the VAO and texture
                GL30C.glBindVertexArray(0);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);

                timeIndex++;
                timeHistory[timeIndex % timeHistory.length] = System.currentTimeMillis();
                double fps = 1000d * timeHistory.length / (timeHistory[timeIndex % timeHistory.length] - timeHistory[(timeIndex + 1) % timeHistory.length]);

                time = GLFW.glfwGetTime();
                if (time - secondTime > 0.5) {
                    secondTime = time;

                    Runtime runtime = Runtime.getRuntime();
                    long totalMemory = runtime.totalMemory();
                    long freeMemory = runtime.freeMemory();
                    long usedMemory = totalMemory - freeMemory;

                    rightDebugText = String.format("GPU: %s\n", GL11.glGetString(GL11.GL_RENDERER));
                    rightDebugText += String.format("Java Version: %s\n", System.getProperty("java.version"));
                    rightDebugText += String.format("OpenGL Version: %s\n", window.getVersion());
                    rightDebugText += String.format("Operating System: %s\n", System.getProperty("os.name"));
                    rightDebugText += String.format("Memory Usage: %,d/%,d (%.2f%%)\n", usedMemory, totalMemory, (double) usedMemory * 100d / totalMemory);

                    world.freeAllChunksNotIn(new ArrayList<>(renderedChunks));
                }

                if (gameState.getItem("seeDebug", Boolean.class)) {
                    StringBuilder ldt = new StringBuilder();
                    ldt.append(ConstantGameSettings.DEFAULT_WINDOW_TITLE);
                    ldt.append("\n");
                    ldt.append(String.format("FPS: %d\n", (int) fps));
                    ldt.append(String.format("Position: %.2f %.2f %.2f\n", -camera.getX(), -camera.getY(), -camera.getZ()));
                    ldt.append(String.format("Chunks:\n\t- Rendered: %d\n\t- Loaded: %d\n\t- Should be loaded: %d\n\t- Bufferized Chunks: %d\n", renderedChunksInFrustum.size(), world.size(), totalRenderedChunks, bufferizedChunkCount));
                    ldt.append(String.format("Network:\n\t- Inflight Requests: %d\n\t- Chunk Requests Sent: %d\n\t- Chunk Requests Received: %d\n", gameState.getItem("inflight_requests", Integer.class), gameState.getItem("chunk_requests_sent", Integer.class), gameState.getItem("chunk_requests_received", Integer.class)));
                    String leftDebugText = ldt.toString();

                    textShaderProgram.bind();

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    GL11.glDisable(GL11.GL_DEPTH_TEST);

                    textRenderer.renderText(font, leftDebugText, 4, 4, 0.75f, Alignment.LEFT);
                    textRenderer.renderText(font, rightDebugText, window.getWidth() - 4, 4, 0.75f, Alignment.RIGHT);

                    GL30C.glBindVertexArray(0);

                    GL11.glEnable(GL11.GL_DEPTH_TEST);

                    GL11.glDisable(GL30C.GL_BLEND);
                }

                client.tick();
                world.tick();

                if (!contextTasks.isEmpty()) {
                    contextTasks.forEach(task -> task.accept(window.window()));
                    contextTasks.clear();
                }

                // Swap the framebuffers
                GLFW.glfwSwapBuffers(window.window());

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                GLFW.glfwPollEvents();
            }

            world.freeAll();
            textRenderer.cleanup();
            font.cleanup();

            GLFW.glfwDestroyWindow(window.window());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            gameRunning.set(false);

            client.close();

            // Terminate GLFW
            GLFW.glfwTerminate();
        }
    }

    private void renderMesh(Position3D position, Mesh mesh, boolean transparent) {
        if (mesh != null) {
            if (mesh instanceof ChunkMesh chunkMesh) {
                renderChunkMesh(position, chunkMesh, transparent);
            } else {
                throw new IllegalArgumentException("A mesh cannot have the type of: " + mesh.getClass().getName());
            }
        }
    }

    private void renderChunkMesh(Position3D position3D, ChunkMesh chunkMesh, boolean transparent) {
        if (transparent) {
            if (chunkMesh.transparentVAO() > 0 && chunkMesh.transparentIndexCount() > 0) {
                shaderProgram.setUniform("chunkPosition", position3D.x(), position3D.y(), position3D.z());
                renderVAO(chunkMesh.transparentVAO(), chunkMesh.transparentIndexCount());
            }
        } else {
            if (chunkMesh.solidVAO() > 0 && chunkMesh.solidIndexCount() > 0) {
                shaderProgram.setUniform("chunkPosition", position3D.x(), position3D.y(), position3D.z());
                renderVAO(chunkMesh.solidVAO(), chunkMesh.solidIndexCount());
            }
        }
    }

    private List<Position3D> calculateRenderedChunks(int renderDistance) {
        int chunkX = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_WIDTH) + 1;
        int chunkY = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_HEIGHT) + 1;
        int chunkZ = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_LENGTH) + 1;

        List<PositionedChunk> newChunks = new ArrayList<>();

        int ccx = (int) Math.floor(camera.getX() / ConstantGameSettings.CHUNK_WIDTH);
        int ccy = (int) Math.floor(camera.getY() / ConstantGameSettings.CHUNK_HEIGHT);
        int ccz = (int) Math.floor(camera.getZ() / ConstantGameSettings.CHUNK_LENGTH);
        for (int x = -chunkX; x < chunkX; x++) {
            for (int y = -chunkY; y < chunkY; y++) {
                for (int z = -chunkZ; z < chunkZ; z++) {
                    int dx = x - ccx;
                    int dy = y - ccy;
                    int dz = z - ccz;
                    int distance = dx * dx + dy * dy + dz * dz;

                    newChunks.add(new PositionedChunk(new Position3D(dx, dy, dz), distance));
                }
            }
        }

        newChunks.sort(Comparator.comparingInt(PositionedChunk::distance));

        return newChunks.stream().map(PositionedChunk::pos).toList();
    }

    private void renderVAO(int vao, int indexCount) {
        // Bind the VAO
        GL30C.glBindVertexArray(vao);

        // Draw the elements using indices in the VAO
        GL30C.glDrawElements(GL11C.GL_TRIANGLES, indexCount, GL11C.GL_UNSIGNED_INT, 0);
    }

    private record PositionedChunk(Position3D pos, int distance) {
    }
}