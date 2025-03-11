package omnivoxel.client.game;

import omnivoxel.client.game.mesh.EntityMesh;
import omnivoxel.client.game.mesh.Mesh;
import omnivoxel.client.game.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.mesh.util.MeshGenerator;
import omnivoxel.client.game.player.camera.Camera;
import omnivoxel.client.game.position.ChangingPosition;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.position.Position;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.shader.ShaderProgram;
import omnivoxel.client.game.shader.ShaderProgramHandler;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.texture.TextureLoader;
import omnivoxel.client.game.window.Window;
import omnivoxel.client.game.window.WindowFactory;
import omnivoxel.client.game.world.World;
import omnivoxel.client.network.Client;
import omnivoxel.debug.Logger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class GameLoop implements Runnable {
    private final Camera camera;
    private final World world;
    private final AtomicBoolean gameRunning;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final Client client;
    private final GameState gameState;
    private final Settings settings;
    private ShaderProgram shaderProgram;

    public GameLoop(Camera camera, World world, AtomicBoolean gameRunning, BlockingQueue<Consumer<Long>> contextTasks, Client client, GameState gameState, Settings settings) {
        this.camera = camera;
        this.world = world;
        this.gameRunning = gameRunning;
        this.contextTasks = contextTasks;
        this.client = client;
        this.gameState = gameState;
        this.settings = settings;
    }

    @Override
    public void run() {
        try {
            Window window = WindowFactory.createWindow(500, 500, ConstantGameSettings.DEFAULT_WINDOW_TITLE, new Logger());
            window.init(500, 500);
            window.show();

            // Initializes the default shader
            // TODO: Make the player able to use their shaders instead.
            ShaderProgramHandler shaderProgramHandler = new ShaderProgramHandler();
            shaderProgramHandler.addShaderProgram("default", Map.of(
                    "assets/shaders/default.vert",
                    GL20.GL_VERTEX_SHADER,
                    "assets/shaders/default.frag",
                    GL20.GL_FRAGMENT_SHADER
            ));
            this.shaderProgram = shaderProgramHandler.getShaderProgram("default");
            shaderProgram.bind();
            shaderProgram.addUniformLocation("view");
            shaderProgram.addUniformLocation("projection");
            shaderProgram.addUniformLocation("chunkPosition");
            shaderProgram.addUniformLocation("exactPosition");
            shaderProgram.addUniformLocation("useChunkPosition");
            shaderProgram.addUniformLocation("useExactPosition");
            shaderProgram.addUniformLocation("cameraPosition");

            // Fog uniforms
            shaderProgram.addUniformLocation("fogColor");
            shaderProgram.addUniformLocation("fogFar");
            shaderProgram.addUniformLocation("fogNear");

            shaderProgram.setUniform("fogColor", 0.0f, 0.61568627451f, 1.0f, 1.0f);
            shaderProgram.setUniform("fogFar", settings.getFloatSetting("render_distance", 100));
            shaderProgram.setUniform("fogNear", settings.getFloatSetting("render_distance", 100) / 2);
            shaderProgram.unbind();

            gameState.setItem("shouldUpdateView", true);
            gameState.setItem("shouldUpdateVisibleMeshes", true);
            gameState.setItem("shouldRenderWireframe", false);

            MeshGenerator meshGenerator = new MeshGenerator();

            GL11C.glClearColor(0.0f, 0.61568627451f, 1.0f, 1.0f);

//            GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_LINE);

            // TODO: Make this stitch textures together and save texture coordinates in a string->(x, y) map.
            // TODO: Make the user be able to use texture packs instead
            int texture = TextureLoader.loadTexture("texture_atlas.png");

            // Enable depth testing for solid chunks
            GL11C.glEnable(GL11C.GL_DEPTH_TEST);

            List<ChunkPosition> renderedChunks = new ArrayList<>();

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

                if (gameState.getItem("shouldUpdateVisibleMeshes", Boolean.class)) {
                    renderedChunks.clear();
                    calculateRenderedChunks(renderedChunks, settings.getIntSetting("render_distance", 100));

                    gameState.setItem("shouldUpdateVisibleMeshes", false);
                }
                renderedChunks.forEach(chunkPosition -> renderMesh(chunkPosition, world.getChunk(chunkPosition), false));

                GL11C.glDepthMask(false);
                GL11C.glEnable(GL11C.GL_BLEND);
                GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
                GL11C.glDisable(GL11C.GL_CULL_FACE);
                renderedChunks.forEach(chunkPosition -> renderMesh(chunkPosition, world.getChunk(chunkPosition), true));
                GL11C.glDisable(GL11C.GL_BLEND);
                GL11C.glDepthMask(true);

                // Render entities
                shaderProgram.setUniform("useChunkPosition", false);
                shaderProgram.setUniform("useExactPosition", true);
                Map<String, EntityMesh> entityMeshes = world.getEntityMeshes();
                for (Map.Entry<String, EntityMesh> entry : entityMeshes.entrySet()) {
                    renderMesh(world.getEntity(entry.getKey()).getPosition(), entry.getValue(), false);
                }

                // Bufferize chunks
                world.bufferizeChunk(meshGenerator, System.nanoTime() + 1000000000);
                world.bufferizeEntity(meshGenerator);

                // Unbind the VAO and texture
                GL30C.glBindVertexArray(0);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);

                if (gameState.getItem("shouldUpdateView", Boolean.class)) {
                    Matrix4f projectionMatrix = getProjectionMatrix(window);
                    Matrix4f viewMatrix = getViewMatrix();

                    camera.getFrustum().updateFrustum(projectionMatrix, viewMatrix);
                    shaderProgram.setUniform("projection", projectionMatrix);
                    shaderProgram.setUniform("view", viewMatrix);

                    gameState.setItem("shouldUpdateView", false);
                }

                // Unbind the shader program
                shaderProgram.unbind();

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

    private void calculateRenderedChunks(List<ChunkPosition> renderedChunks, int renderDistance) {
        List<ChunkPosition> chunks = new ArrayList<>();
        int chunkX = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_WIDTH);
        int chunkY = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_HEIGHT);
        int chunkZ = Math.round((float) renderDistance / ConstantGameSettings.CHUNK_LENGTH);

        int ccx = (int) Math.floor(camera.getX() / ConstantGameSettings.CHUNK_WIDTH);
        int ccy = (int) Math.floor(camera.getY() / ConstantGameSettings.CHUNK_HEIGHT);
        int ccz = (int) Math.floor(camera.getZ() / ConstantGameSettings.CHUNK_LENGTH);
        for (int x = -chunkX; x < chunkX; x++) {
            for (int y = -chunkY; y < chunkY; y++) {
                for (int z = -chunkZ; z < chunkZ; z++) {
                    chunks.add(new ChunkPosition(x - ccx, y - ccy, z - ccz));
                }
            }
        }
        chunks.sort(Comparator.comparingInt(chunkPosition -> {
            int distanceInt = chunkPosition.x() * chunkPosition.x() + chunkPosition.y() * chunkPosition.y() + chunkPosition.z() * chunkPosition.z();
            if (camera.getFrustum().isMeshInFrustum(chunkPosition)) {
                distanceInt -= Integer.MAX_VALUE;
            }
            return distanceInt;
        }));
        chunks.forEach(chunkPosition -> {
            if (camera.getFrustum().isMeshInFrustum(chunkPosition)) {
                renderedChunks.add(chunkPosition);
            }
        });
    }

    private void renderMesh(Position position, Mesh mesh, boolean transparent) {
        if (mesh != null) {
            if (mesh instanceof ChunkMesh chunkMesh) {
                renderChunkMesh((ChunkPosition) position, chunkMesh, transparent);
            } else if (mesh instanceof EntityMesh entityMesh) {
                renderEntityMesh((ChangingPosition) position, entityMesh, transparent);
            } else {
                throw new IllegalArgumentException("A mesh cannot have the type of: " + mesh.getClass().getName());
            }
        }
    }

    private void renderEntityMesh(ChangingPosition changingPosition, EntityMesh entityMesh, boolean transparent) {
        shaderProgram.setUniform("exactPosition", changingPosition.x(), changingPosition.y(), changingPosition.z());
        if (transparent) {
            renderVAO(entityMesh.transparentVAO(), entityMesh.transparentIndexCount());
        } else {
            renderVAO(entityMesh.solidVAO(), entityMesh.solidIndexCount());
        }
    }

    private void renderChunkMesh(ChunkPosition chunkPosition, ChunkMesh chunkMesh, boolean transparent) {
        shaderProgram.setUniform("chunkPosition", chunkPosition.x(), chunkPosition.y(), chunkPosition.z());
        if (transparent) {
            renderVAO(chunkMesh.transparentVAO(), chunkMesh.transparentIndexCount());
        } else {
            renderVAO(chunkMesh.solidVAO(), chunkMesh.solidIndexCount());
        }
    }

    private void renderVAO(int vao, int indexCount) {
        if (vao > 0 && indexCount > 0) {
            // Bind the VAO
            GL30C.glBindVertexArray(vao);

            // Draw the elements using indices in the VAO
            GL30C.glDrawElements(GL11C.GL_TRIANGLES, indexCount, GL11C.GL_UNSIGNED_INT, 0);
        }
    }

    private Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotate(camera.getPitch(), 1, 0, 0)
                .rotate(camera.getYaw(), 0, 1, 0)
                .translate(
                        camera.getX(),
                        camera.getY(),
                        camera.getZ()
                );
    }

    public Matrix4f getProjectionMatrix(Window window) {
        return new Matrix4f()
                .setPerspective(
                        (float) Math.toRadians(camera.getFov()),
                        window.aspectRatio(),
                        camera.getNear(),
                        camera.getFar()
                );
    }
}