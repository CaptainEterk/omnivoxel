package omnivoxel.client.game;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.Mesh;
import omnivoxel.client.game.graphics.opengl.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.graphics.opengl.mesh.util.MeshGenerator;
import omnivoxel.client.game.graphics.opengl.shader.ShaderProgram;
import omnivoxel.client.game.graphics.opengl.shader.ShaderProgramHandler;
import omnivoxel.client.game.graphics.opengl.text.Alignment;
import omnivoxel.client.game.graphics.opengl.text.TextRenderer;
import omnivoxel.client.game.graphics.opengl.text.font.Font;
import omnivoxel.client.game.graphics.opengl.texture.TextureLoader;
import omnivoxel.client.game.graphics.opengl.window.Window;
import omnivoxel.client.game.graphics.opengl.window.WindowFactory;
import omnivoxel.client.game.position.DistanceChunk;
import omnivoxel.client.game.position.PositionedChunk;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.game.world.ClientWorldChunk;
import omnivoxel.client.network.Client;
import omnivoxel.math.Position3D;
import omnivoxel.server.ConstantServerSettings;
import omnivoxel.util.log.Logger;
import omnivoxel.util.time.Timer;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class GameLoop {
    private static final int FPS_SAMPLES = 60;
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f().identity();
    private final Camera camera;
    private final ClientWorld world;
    private final AtomicBoolean gameRunning;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final Client client;
    private final GameState gameState;
    private final Settings settings;
    private final TextRenderer textRenderer;
    private final long[] timeHistory = new long[FPS_SAMPLES];
    private final Logger logger;
    private ShaderProgram shaderProgram;
    private int timeIndex = 0;
    private ShaderProgram zppShaderProgram;
    private Font font;

    // TODO: Separate tasks

    public GameLoop(Camera camera, ClientWorld world, AtomicBoolean gameRunning, BlockingQueue<Consumer<Long>> contextTasks, Client client, GameState gameState, Settings settings, TextRenderer textRenderer) {
        this.camera = camera;
        this.world = world;
        this.gameRunning = gameRunning;
        this.contextTasks = contextTasks;
        this.client = client;
        this.gameState = gameState;
        this.settings = settings;
        this.textRenderer = textRenderer;
        this.logger = new Logger("game_loop");
        logger.addTimer("shouldUpdateVisibleMeshes", new Timer("shouldUpdateVisibleMeshes", 50));
        logger.addTimer("shouldUpdateView", new Timer("shouldUpdateView", 50));
        logger.addTimer("other", new Timer("other", 50));
    }

    public void run() {
        try {
            Window window = WindowFactory.createWindow(500, 500, ConstantGameSettings.DEFAULT_WINDOW_TITLE, logger);

            // Initializes the default shader
            // TODO: Make the player able to use their shaders instead.
            ShaderProgramHandler shaderProgramHandler = new ShaderProgramHandler();
            shaderProgramHandler.addShaderProgram("default", Map.of("assets/shaders/default.vert", GL20.GL_VERTEX_SHADER, "assets/shaders/default.frag", GL20.GL_FRAGMENT_SHADER));
            shaderProgramHandler.addShaderProgram("zpp", Map.of("assets/shaders/zpp.vert", GL20.GL_VERTEX_SHADER, "assets/shaders/zpp.frag", GL20.GL_FRAGMENT_SHADER));
            shaderProgramHandler.addShaderProgram("text", Map.of("assets/shaders/text.vert", GL20.GL_VERTEX_SHADER, "assets/shaders/text.frag", GL20.GL_FRAGMENT_SHADER));
            this.shaderProgram = shaderProgramHandler.getShaderProgram("default");
            this.zppShaderProgram = shaderProgramHandler.getShaderProgram("zpp");
            ShaderProgram textShaderProgram = shaderProgramHandler.getShaderProgram("text");
            shaderProgram.bind();
            shaderProgram.setUniform("fogColor", 0.0f, 0.61568627451f, 1.0f, 1.0f);
            shaderProgram.setUniform("fogFar", settings.getFloatSetting("render_distance", 100));
            shaderProgram.setUniform("fogNear", settings.getFloatSetting("render_distance", 100) / 10 * 9);
            shaderProgram.setUniform("texture1", 0);
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
            gameState.setItem("missing_chunks", 0);

            gameState.setItem("z-prepass", false);

            gameState.setItem("inflight_requests", 0);
            gameState.setItem("chunk_requests_sent", 0);
            gameState.setItem("chunk_requests_received", 0);

            textRenderer.init();

            font = Font.create("Minecraft.ttf");

            MeshGenerator meshGenerator = new MeshGenerator();

            GL11C.glClearColor(0.0f, 0.61568627451f, 1.0f, 1.0f);

            // TODO: Make this stitch textures together and save texture coordinates in a string->(x, y) map.
            // TODO: Make the user be able to use texture packs instead (by loading it and stitching it together)
            int texture = TextureLoader.loadTexture("texture_atlas.png");
            int TEMP_texture = TextureLoader.loadTexture("player_texture.png");

            // Enable depth testing for solid chunks
            GL11C.glEnable(GL11C.GL_CULL_FACE);
            GL11C.glCullFace(GL11C.GL_BACK);

            List<DistanceChunk> solidRenderedChunks = new ArrayList<>();
            List<DistanceChunk> transparentRenderedChunks = new ArrayList<>();
            int totalRenderedChunks = 1;

            window.init(500, 500);
            window.show();

            double time = GLFW.glfwGetTime();

            String rightDebugText = "";
            double secondTime = time;

            boolean completeRenderDistance = false;

            AtomicBoolean recalculateRenderedChunks = new AtomicBoolean(true);

            // Renders everything
            while (!window.shouldClose()) {
                // Clear the framebuffer
                GL11.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

                shaderProgram.bind();
                GL13C.glActiveTexture(GL13C.GL_TEXTURE0);

                int trc = update(window, solidRenderedChunks, transparentRenderedChunks, world.size() >= totalRenderedChunks);
                if (trc > -1) {
                    totalRenderedChunks = trc;
                }

                // RENDER

                GL11C.glEnable(GL11C.GL_DEPTH_TEST);
                GL11C.glEnable(GL11C.GL_CULL_FACE);

                // Render entities
                GL11C.glDepthFunc(GL11C.GL_LEQUAL);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, TEMP_texture);
                shaderProgram.setUniform("meshType", 1);
                Map<String, ClientEntity> entityMeshes = world.getEntities();
                entityMeshes.forEach((id, clientEntity) -> renderEntityMesh(clientEntity.getMesh(), IDENTITY_MATRIX));

                List<PositionedChunk> solidRenderedChunksInFrustum = new ArrayList<>((int) (solidRenderedChunks.size() * (camera.getFOV() / 360.0)));
                for (DistanceChunk solidRenderedChunk : solidRenderedChunks) {
                    if (camera.getFrustum().isMeshInFrustum(solidRenderedChunk.pos())) {
                        solidRenderedChunksInFrustum.add(new PositionedChunk(solidRenderedChunk.pos(), world.get(solidRenderedChunk.pos(), false)));
                    }
                }

                List<PositionedChunk> transparentRenderedChunksInFrustum = new ArrayList<>((int) (transparentRenderedChunks.size() * (camera.getFOV() / 360.0)));
                for (DistanceChunk transparentRenderedChunk : transparentRenderedChunks) {
                    if (camera.getFrustum().isMeshInFrustum(transparentRenderedChunk.pos())) {
                        transparentRenderedChunksInFrustum.add(new PositionedChunk(transparentRenderedChunk.pos(), world.get(transparentRenderedChunk.pos(), false)));
                    }
                }

                // Render chunks
                shaderProgram.setUniform("meshType", 0);
                shaderProgram.setUniform("model", IDENTITY_MATRIX);
//
                if (gameState.getItem("z-prepass", Boolean.class)) {
                    zppShaderProgram.bind();
                    zppShaderProgram.setUniform("meshType", 0);
                    zppShaderProgram.setUniform("model", IDENTITY_MATRIX);

                    GL11C.glDepthFunc(GL11C.GL_LESS);
                    GL11C.glColorMask(false, false, false, false);

                    solidRenderedChunksInFrustum.forEach(positionedChunk -> renderMesh(positionedChunk.pos(), positionedChunk.chunk().getMesh(), false));

                    GL11C.glDepthFunc(GL11C.GL_EQUAL);

                    GL11C.glColorMask(true, true, true, true);

                    shaderProgram.bind();
                } else {
                    GL11C.glDepthFunc(GL11C.GL_LEQUAL);
                }

                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);

                // Color pass
                solidRenderedChunksInFrustum.forEach(positionedChunk -> renderMesh(positionedChunk.pos(), positionedChunk.chunk().getMesh(), false));

                // Transparent Chunk Meshes
                GL11C.glDepthFunc(GL11C.GL_LESS);
                GL11C.glDisable(GL11C.GL_CULL_FACE);
                GL11C.glDepthMask(false);
                GL11C.glEnable(GL11C.GL_BLEND);
                GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
                for (int i = transparentRenderedChunksInFrustum.size() - 1; i >= 0; i--) {
                    PositionedChunk positionedChunk = transparentRenderedChunksInFrustum.get(i);
                    renderMesh(positionedChunk.pos(), positionedChunk.chunk().getMesh(), true);
                }
                GL11C.glDisable(GL11C.GL_BLEND);
                GL11C.glEnable(GL11C.GL_CULL_FACE);

                // Bufferize chunks
                // TODO: Make the bufferizer actually use the endTime
                int bufferizedChunkCount = world.bufferizeQueued(meshGenerator, System.nanoTime());

                // Unbind the VAO and texture
                GL30C.glBindVertexArray(0);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);

                timeIndex++;
                timeHistory[timeIndex % timeHistory.length] = System.currentTimeMillis();
                double fps = 1000d * timeHistory.length / (timeHistory[timeIndex % timeHistory.length] - timeHistory[(timeIndex + 1) % timeHistory.length]);

                time = GLFW.glfwGetTime();
                if (time - secondTime > 0.5) {
                    secondTime += 0.5;

                    Runtime runtime = Runtime.getRuntime();
                    long totalMemory = runtime.totalMemory();
                    long freeMemory = runtime.freeMemory();
                    long usedMemory = totalMemory - freeMemory;

                    rightDebugText = String.format("GPU: %s\n", GL11.glGetString(GL11.GL_RENDERER));
                    rightDebugText += String.format("Java Version: %s\n", System.getProperty("java.version"));
                    rightDebugText += String.format("OpenGL Version: %s\n", window.getVersion());
                    rightDebugText += String.format("Operating System: %s %s\n", System.getProperty("os.name"), System.getProperty("os.version"));
                    rightDebugText += String.format("Memory Usage: %,d/%,d (%.2f%%)\n", usedMemory, totalMemory, (double) usedMemory * 100d / totalMemory);
                }

                if (gameState.getItem("seeDebug", Boolean.class)) {
                    String leftDebugText = ConstantGameSettings.DEFAULT_WINDOW_TITLE + "\n" + String.format("FPS: %d\nPosition: %.2f %.2f %.2f\nChunks:\n\t- Rendered: %d/%d/%d\n\t- Loaded: %d\n\t- Should be loaded: %d\n\t- Bufferized Chunks: %d\n\t- Missing Chunks: %d\nNetwork:\n\t- Inflight Requests: %d\n\t- Chunk Requests Sent: %d\n\t- Chunk Requests Received: %d\n", (int) fps, camera.getX(), camera.getY(), camera.getZ(), solidRenderedChunksInFrustum.size() + transparentRenderedChunksInFrustum.size(), solidRenderedChunksInFrustum.size(), transparentRenderedChunksInFrustum.size(), world.size(), totalRenderedChunks, bufferizedChunkCount, gameState.getItem("missing_chunks", Integer.class), gameState.getItem("inflight_requests", Integer.class), gameState.getItem("chunk_requests_sent", Integer.class), gameState.getItem("chunk_requests_received", Integer.class));

                    textShaderProgram.bind();

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    GL11.glDisable(GL11.GL_DEPTH_TEST);

                    textRenderer.renderText(font, leftDebugText, 4, 4, 0.6f, Alignment.LEFT);
                    textRenderer.renderText(font, rightDebugText, window.getWidth() - 4, 4, 0.6f, Alignment.RIGHT);

                    GL30C.glBindVertexArray(0);

                    GL11.glEnable(GL11.GL_DEPTH_TEST);

                    GL11.glDisable(GL30C.GL_BLEND);
                }

                GL11C.glDepthMask(true);

                client.tick();
                world.tick();

                Consumer<Long> task;
                while ((task = contextTasks.poll()) != null) {
                    task.accept(window.window());
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

    private void renderEntityMesh(EntityMesh entityMesh, Matrix4f parentTransform) {
        if (entityMesh != null) {
            Matrix4f currentTransform = new Matrix4f(parentTransform).mul(entityMesh.getModel());
            shaderProgram.setUniform("model", currentTransform);
            renderVAO(entityMesh.getDefinition().solidVAO(), entityMesh.getDefinition().solidIndexCount());
            entityMesh.getChildren().forEach(mesh -> renderEntityMesh(mesh, currentTransform));
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

    private List<DistanceChunk> calculateRenderedChunks(int renderDistance) {
        int chunkX = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_WIDTH) + 1;
        int chunkY = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_HEIGHT) + 1;
        int chunkZ = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_LENGTH) + 1;

        Map<Integer, Queue<DistanceChunk>> positionedChunks = new HashMap<>();
        int highestBucketDistance = 0;

        int ccx = (int) -Math.floor(camera.getX() / ConstantGameSettings.CHUNK_WIDTH);
        int ccy = (int) -Math.floor(camera.getY() / ConstantGameSettings.CHUNK_HEIGHT);
        int ccz = (int) -Math.floor(camera.getZ() / ConstantGameSettings.CHUNK_LENGTH);
        int count = 0;

//        System.out.println(ccx + " " + ccy + " " + ccz);

        for (int x = -chunkX; x <= chunkX; x++) {
            for (int y = -chunkY; y <= chunkY; y++) {
                for (int z = -chunkZ; z <= chunkZ; z++) {
                    int dx = x - ccx;
                    int dy = y - ccy;
                    int dz = z - ccz;
                    int distance = x * x + y * y + z * z;

                    if (distance > highestBucketDistance) {
                        highestBucketDistance = distance;
                    }

                    if (distance < renderDistance) {
                        Position3D position3D = new Position3D(dx, dy, dz);
                        positionedChunks.computeIfAbsent(distance, i -> new ArrayDeque<>()).add(new DistanceChunk(distance, position3D));
                        count++;
                    }
                }
            }
        }

        List<DistanceChunk> out = new ArrayList<>(count);

        for (int i = 0; i < highestBucketDistance; i++) {
            Queue<DistanceChunk> posChunks = positionedChunks.get(i);
            if (posChunks != null) {
                out.addAll(posChunks);
            }
        }

        return out;
    }

    private void renderVAO(int vao, int indexCount) {
        // Bind the VAO
        GL30C.glBindVertexArray(vao);

        // Draw the elements using indices in the VAO
        GL30C.glDrawElements(GL11C.GL_TRIANGLES, indexCount, GL11C.GL_UNSIGNED_INT, 0);
    }

    private int update(Window window, List<DistanceChunk> solidRenderedChunks, List<DistanceChunk> transparentRenderedChunks, boolean completeRenderDistance) {
        int totalRenderedChunks = -1;
        // Render wireframe or not
        if (gameState.getItem("shouldRenderWireframe", Boolean.class)) {
            GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_LINE);
        } else {
            GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);
        }

        // Set camera position uniforms
        shaderProgram.setUniform("cameraPosition", camera.getX(), camera.getY(), camera.getZ());

        if (gameState.getItem("shouldUpdateView", Boolean.class)) {
            Matrix4f projectionMatrix = new Matrix4f().setPerspective((float) Math.toRadians(camera.getFOV()), window.aspectRatio(), camera.getNear(), camera.getFar());
            Matrix4f viewMatrix = new Matrix4f().rotate(camera.getPitch(), 1, 0, 0).rotate(camera.getYaw(), 0, 1, 0).translate(-camera.getX(), -camera.getY(), -camera.getZ());

            camera.updateFrustum(projectionMatrix, viewMatrix);
            shaderProgram.setUniform("projection", projectionMatrix);
            shaderProgram.setUniform("view", viewMatrix);

            zppShaderProgram.bind();
            zppShaderProgram.setUniform("projection", projectionMatrix);
            zppShaderProgram.setUniform("view", viewMatrix);
            shaderProgram.bind();

            gameState.setItem("shouldUpdateView", false);
        }
        if (world.totalQueuedChunks() < ConstantServerSettings.QUEUED_CHUNKS_MINIMUM && !completeRenderDistance) {
            gameState.setItem("shouldUpdateVisibleMeshes", true);
        }
        if (gameState.getItem("shouldUpdateVisibleMeshes", Boolean.class)) {
            solidRenderedChunks.clear();
            transparentRenderedChunks.clear();

            int ccx = (int) Math.floor(camera.getX() / ConstantGameSettings.CHUNK_WIDTH);
            int ccy = (int) Math.floor(camera.getY() / ConstantGameSettings.CHUNK_HEIGHT);
            int ccz = (int) Math.floor(camera.getZ() / ConstantGameSettings.CHUNK_LENGTH);

            int renderDistance = settings.getIntSetting("render_distance", 100);
            int squaredRenderDistance = renderDistance * renderDistance;

            List<DistanceChunk> chunks = calculateRenderedChunks(renderDistance);
            world.freeAllChunksNotIn(position3D -> {
                int dx = position3D.x() - ccx;
                int dy = position3D.y() - ccy;
                int dz = position3D.z() - ccz;
                int distance = dx * dx + dy * dy + dz * dz;

                return distance < squaredRenderDistance;
            });

            totalRenderedChunks = chunks.size();

            // Filter chunks
            for (DistanceChunk chunk : chunks) {
                ClientWorldChunk clientWorldChunk = world.get(chunk.pos(), true);
                if (clientWorldChunk != null && clientWorldChunk.getMesh() != null) {
                    if (clientWorldChunk.getMesh().solidIndexCount() > 0) {
                        solidRenderedChunks.add(chunk);
                    }
                    if (clientWorldChunk.getMesh().transparentIndexCount() > 0) {
                        transparentRenderedChunks.add(chunk);
                    }
                }
            }

            // TODO: Make it so that
            transparentRenderedChunks.sort(Comparator.comparingInt(DistanceChunk::distance));

            gameState.setItem("shouldUpdateVisibleMeshes", false);
        }

        return totalRenderedChunks;
    }
}