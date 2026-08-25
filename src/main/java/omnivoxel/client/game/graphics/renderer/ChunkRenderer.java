package omnivoxel.client.game.graphics.renderer;

import omnivoxel.client.game.graphics.RendererAPI;
import omnivoxel.client.game.graphics.api.opengl.OpenGLChecks;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkIndirectBuffer;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkMeshBuffer;
import omnivoxel.client.game.graphics.api.opengl.shader.ShaderProgram;
import omnivoxel.client.game.graphics.api.opengl.shader.ShaderProgramHandler;
import omnivoxel.client.game.graphics.api.opengl.texture.TextureLoader;
import omnivoxel.client.game.graphics.camera.Camera;
import omnivoxel.client.game.graphics.chunk.RenderedChunkProvider;
import omnivoxel.client.game.position.DistanceChunk;
import omnivoxel.client.game.position.PositionedChunk;
import omnivoxel.client.game.state.State;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.common.settings.Settings;
import omnivoxel.util.math.Position3D;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ChunkRenderer {
    private static final int CHUNK_GPU_SIZE = 32;
    private final RendererAPI rendererAPI;
    private final State state;
    private final Settings settings;
    private final List<PositionedChunk> solidRenderedChunksInFrustum = new ArrayList<>();
    private final List<PositionedChunk> decorationRenderedChunksInFrustum = new ArrayList<>();
    private final List<PositionedChunk> transparentRenderedChunksInFrustum = new ArrayList<>();
    private final List<DistanceChunk> solidRenderedChunks = new ArrayList<>();
    private final List<DistanceChunk> decorationRenderedChunks = new ArrayList<>();
    private final List<DistanceChunk> transparentRenderedChunks = new ArrayList<>();
    private final Camera camera;
    private final ClientWorld world;
    private final RenderedChunkProvider renderedChunkProvider;
    private final ShaderProgramHandler shaderProgramHandler;
    private final ChunkIndirectMemoryManager chunkIndirectMemoryManager;
    private int texture;
    private ShaderProgram chunkCullingComputeShaderProgram;
    private int chunkBuffer;
    private int chunkBufferCapacity;
    private ChunkMeshBuffer chunkMeshBuffer;
    private ChunkIndirectBuffer chunkIndirectBuffer;
    private int indirectDrawCount;

    public ChunkRenderer(RendererAPI rendererAPI, State state, Settings settings, Camera camera, ClientWorld world, RenderedChunkProvider renderedChunkProvider) {
        this.rendererAPI = rendererAPI;
        this.state = state;
        this.settings = settings;
        this.camera = camera;
        this.world = world;
        this.renderedChunkProvider = renderedChunkProvider;
        this.shaderProgramHandler = rendererAPI.getShaderProgramHandler();
        chunkIndirectMemoryManager = new ChunkIndirectMemoryManager();
    }

    public void initResources(ChunkMeshBuffer chunkMeshBuffer, ChunkIndirectBuffer chunkIndirectBuffer) throws IOException {
        this.chunkMeshBuffer = chunkMeshBuffer;
        this.chunkIndirectBuffer = chunkIndirectBuffer;
        this.texture = TextureLoader.loadTexture("texture_atlas.png");
        this.chunkCullingComputeShaderProgram = shaderProgramHandler.addShaderProgram("chunk_indirect", Map.of("assets/shaders/chunk_indirect.comp", GL43C.GL_COMPUTE_SHADER));
    }

    private void ensureChunkBuffer(int chunkCount) {
        if (chunkCount == 0) {
            return;
        }

        if (chunkCount <= chunkBufferCapacity && chunkBuffer != 0) {
            return;
        }

        if (chunkBuffer != 0) {
            GL45C.glDeleteBuffers(chunkBuffer);
        }

        chunkBufferCapacity = chunkCount;
        chunkBuffer = GL45C.glCreateBuffers();

        GL45C.glNamedBufferStorage(
                chunkBuffer,
                (long) chunkBufferCapacity * CHUNK_GPU_SIZE,
                GL45C.GL_DYNAMIC_STORAGE_BIT
        );
    }

    public void cleanup() {
        chunkCullingComputeShaderProgram.cleanup();
    }

    public void render() {
        if (state.getItem("shouldUpdateVisibleMeshes", Boolean.class)) {
            solidRenderedChunks.clear();
            decorationRenderedChunks.clear();
            transparentRenderedChunks.clear();

            int renderDistance = settings.getIntSetting("render_distance", 100);
            float distanceLod0 = settings.getFloatSetting("distance_lod0", 1.0f);
            float distanceLod1 = settings.getFloatSetting("distance_lod1", 1.0f);
            float distanceLod2 = settings.getFloatSetting("distance_lod2", 1.0f);
            float distanceLod3 = settings.getFloatSetting("distance_lod3", 1.0f);
            float distanceLod4 = settings.getFloatSetting("distance_lod4", 1.0f);

            renderedChunkProvider.update(settings.getIntSetting("frustum_bias", 10), renderDistance, camera);
            List<DistanceChunk> chunks = renderedChunkProvider.getOutput();

            if (!chunks.isEmpty()) {
                int rdChunks = renderDistance / ConstantCommonSettings.CHUNK_SIZE + 1;
                float squaredRenderDistance = rdChunks * rdChunks;

                ensureChunkBuffer(chunks.size());

                ByteBuffer chunkData = MemoryUtil.memAlloc(chunks.size() * CHUNK_GPU_SIZE);

                try {
                    for (DistanceChunk chunk : chunks) {
                        int lod;

                        float d = chunk.distance() / squaredRenderDistance;

                        if (d < distanceLod0) {
                            lod = 0;
                        } else if (d < distanceLod1) {
                            lod = 1;
                        } else if (d < distanceLod2) {
                            lod = 2;
                        } else if (d < distanceLod3) {
                            lod = 3;
                        } else if (d < distanceLod4) {
                            lod = 4;
                        } else {
                            lod = 5;
                        }

                        Position3D position = chunk.pos();

                        var clientChunk = world.get(position, true, false);

                        chunkData.putInt(position.x());
                        chunkData.putInt(position.y());
                        chunkData.putInt(position.z());

                        if (clientChunk == null || clientChunk.getMesh() == null) {
                            chunkData.putInt(0);
                            chunkData.putInt(0);
                            chunkData.putInt(0);
                            chunkData.putInt(0);
                        } else {
                            var mesh = clientChunk.getMesh().solid();
                            if (mesh != null) {
                                chunkData.putInt(clientChunk.getMesh().lod());
                                chunkData.putInt(mesh.indexCount());
                                chunkData.putInt(mesh.firstIndex());
                                chunkData.putInt(mesh.baseVertex());
                            } else {
                                chunkData.putInt(0);
                                chunkData.putInt(0);
                                chunkData.putInt(0);
                                chunkData.putInt(0);
                            }
                        }
                        chunkData.putInt(0);
                    }

                    chunkData.flip();

                    GL45C.glNamedBufferSubData(
                            chunkBuffer,
                            0,
                            chunkData
                    );
                } finally {
                    MemoryUtil.memFree(chunkData);
                }

                indirectDrawCount = chunks.size();

//                System.out.println(indirectDrawCount);

                chunkCullingComputeShaderProgram.bind();

                chunkCullingComputeShaderProgram.setUniformUnsigned(
                        "chunkCount",
                        chunks.size()
                );

                GL43C.glBindBufferBase(
                        GL43C.GL_SHADER_STORAGE_BUFFER,
                        0,
                        chunkBuffer
                );

                GL43C.glBindBufferBase(
                        GL43C.GL_SHADER_STORAGE_BUFFER,
                        1,
                        chunkIndirectBuffer.buffer()
                );

                int workGroups = (chunks.size() + 63) / 64;

                GL43C.glDispatchCompute(
                        workGroups,
                        1,
                        1
                );

                GL43C.glMemoryBarrier(
                        GL43C.GL_COMMAND_BARRIER_BIT |
                                GL43C.GL_SHADER_STORAGE_BARRIER_BIT
                );

                transparentRenderedChunks.sort(Comparator.comparingInt(DistanceChunk::distance));

                OpenGLChecks.checkError("indirect");
            }
        }

        System.out.println(indirectDrawCount);

        state.setItem("total_rendered_chunks", indirectDrawCount);
        state.setItem("shouldUpdateVisibleMeshes", false);
        shaderProgramHandler.getShaderProgram("default").bind();
        shaderProgramHandler.getShaderProgram("default").setUniformUnsigned("meshType", 0);

        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
        GL11C.glDepthFunc(GL11C.GL_LEQUAL);
        GL11C.glDepthMask(true);

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                texture
        );

        GL45C.glBindVertexArray(
                chunkMeshBuffer.vao()
        );

        GL43C.glBindBufferBase(
                GL43C.GL_SHADER_STORAGE_BUFFER,
                0,
                chunkBuffer
        );

        GL45C.glBindBuffer(
                GL43C.GL_DRAW_INDIRECT_BUFFER,
                chunkIndirectBuffer.buffer()
        );

        GL45C.glMultiDrawElementsIndirect(
                GL11C.GL_TRIANGLES,
                GL11C.GL_UNSIGNED_INT,
                0L,
                indirectDrawCount,
                0
        );

        OpenGLChecks.checkError("chunks");
    }
}