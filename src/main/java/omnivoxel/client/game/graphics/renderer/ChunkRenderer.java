package omnivoxel.client.game.graphics.renderer;

import omnivoxel.client.game.graphics.RenderType;
import omnivoxel.client.game.graphics.RendererAPI;
import omnivoxel.client.game.graphics.api.opengl.OpenGLChecks;
import omnivoxel.client.game.graphics.api.opengl.texture.TextureLoader;
import omnivoxel.client.game.graphics.camera.Camera;
import omnivoxel.client.game.graphics.chunk.RenderedChunkProvider;
import omnivoxel.client.game.position.DistanceChunk;
import omnivoxel.client.game.position.PositionedChunk;
import omnivoxel.client.game.state.State;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.game.world.ClientWorldChunk;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.common.settings.Settings;
import omnivoxel.util.math.Position3D;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChunkRenderer {
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
    private int texture;

    public ChunkRenderer(RendererAPI rendererAPI, State state, Settings settings, Camera camera, ClientWorld world, RenderedChunkProvider renderedChunkProvider) {
        this.rendererAPI = rendererAPI;
        this.state = state;
        this.settings = settings;
        this.camera = camera;
        this.world = world;
        this.renderedChunkProvider = renderedChunkProvider;
    }

    public void initResources() {
        this.texture = TextureLoader.loadTexture("texture_atlas.png");
    }

    public void render() {
        if (state.getItem("shouldUpdateVisibleMeshes", Boolean.class)) {
            solidRenderedChunks.clear();
            decorationRenderedChunks.clear();
            transparentRenderedChunks.clear();

            int renderDistance = settings.getIntSetting("render_distance", 100);

            renderedChunkProvider.update(settings.getIntSetting("frustum_bias", 10), renderDistance, camera);
            List<DistanceChunk> chunks = renderedChunkProvider.getOutput();

            int rdChunks = renderDistance / ConstantCommonSettings.CHUNK_SIZE + 1;
            int squaredRenderDistance = rdChunks * rdChunks;

            // TODO: This is an expensive operation, optimize it
            for (DistanceChunk chunk : chunks) {
                int lod;

                if (chunk.distance() < squaredRenderDistance / 32) {
                    lod = 0;
                } else if (chunk.distance() < squaredRenderDistance / 16) {
                    lod = 1;
                } else if (chunk.distance() < squaredRenderDistance / 8) {
                    lod = 2;
                } else if (chunk.distance() < squaredRenderDistance / 4) {
                    lod = 3;
                } else if (chunk.distance() < squaredRenderDistance / 2) {
                    lod = 4;
                } else {
                    lod = 5;
                }

                ClientWorldChunk clientWorldChunk = world.get(chunk.pos(), true, false, lod);
                if (clientWorldChunk != null && clientWorldChunk.getMesh() != null) {
                    if (clientWorldChunk.getMesh().solid().indexCount() > 0) {
                        solidRenderedChunks.add(chunk);
                    }
                    if (clientWorldChunk.getMesh().decoration().indexCount() > 0) {
                        decorationRenderedChunks.add(chunk);
                    }
                    if (clientWorldChunk.getMesh().transparent().indexCount() > 0) {
                        transparentRenderedChunks.add(chunk);
                    }
                }
            }

            transparentRenderedChunks.sort(Comparator.comparingInt(DistanceChunk::distance));

            state.setItem("total_rendered_chunks", chunks.size());

            state.setItem("shouldUpdateVisibleMeshes", false);
        }

        calculateFrustumChunks();

        renderSolidChunks();
        renderDecorationChunks();
        renderTransparentChunks();

        OpenGLChecks.checkError("chunks");
    }

    private void calculateFrustumChunks() {
        solidRenderedChunksInFrustum.clear();
        for (DistanceChunk solidRenderedChunk : solidRenderedChunks) {
            if (camera.getFrustum().isChunkInFrustum(solidRenderedChunk.pos())) {
                solidRenderedChunksInFrustum.add(new PositionedChunk(solidRenderedChunk.pos(), world.get(solidRenderedChunk.pos(), false, false)));
            }
        }

        decorationRenderedChunksInFrustum.clear();
        for (DistanceChunk decorationRenderedChunk : decorationRenderedChunks) {
            if (camera.getFrustum().isChunkInFrustum(decorationRenderedChunk.pos())) {
                decorationRenderedChunksInFrustum.add(new PositionedChunk(decorationRenderedChunk.pos(), world.get(decorationRenderedChunk.pos(), false, false)));
            }
        }

        transparentRenderedChunksInFrustum.clear();
        for (DistanceChunk transparentRenderedChunk : transparentRenderedChunks) {
            if (camera.getFrustum().isChunkInFrustum(transparentRenderedChunk.pos())) {
                transparentRenderedChunksInFrustum.add(new PositionedChunk(transparentRenderedChunk.pos(), world.get(transparentRenderedChunk.pos(), false, false)));
            }
        }
    }

    private void renderSolidChunks() {
        rendererAPI.setRenderType(RenderType.CHUNK);

        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
        GL11C.glDepthFunc(GL11C.GL_LEQUAL);
        GL11C.glDepthMask(true);

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);
        int occluded = 0;

        for (PositionedChunk positionedChunk : solidRenderedChunksInFrustum) {
            if (positionedChunk.chunk().getMesh().solid().vao() > 0 && positionedChunk.chunk().getMesh().solid().indexCount() > 0) {
                Position3D position3D = positionedChunk.pos();
                rendererAPI.setShaderIVec3("chunkPosition", position3D.x(), position3D.y(), position3D.z());
                rendererAPI.setShaderInt("chunkScale", 1 << positionedChunk.chunk().getMesh().lod());
                rendererAPI.render(positionedChunk.chunk().getMesh().solid());
            } else {
                occluded++;
            }
        }

        state.setItem("geometry_culled_chunks", occluded);
    }

    private void renderDecorationChunks() {
        rendererAPI.setMipmapping(false);
        GL11C.glDepthFunc(GL11C.GL_LEQUAL);
        GL11C.glDisable(GL11C.GL_CULL_FACE);
        for (PositionedChunk positionedChunk : decorationRenderedChunksInFrustum) {
            Position3D position3D = positionedChunk.pos();
            if (positionedChunk.chunk().getMesh().decoration().vao() > 0 && positionedChunk.chunk().getMesh().decoration().indexCount() > 0) {
                rendererAPI.setShaderIVec3("chunkPosition", position3D.x(), position3D.y(), position3D.z());
                rendererAPI.setShaderInt("chunkScale", 1 << positionedChunk.chunk().getMesh().lod());
                rendererAPI.render(positionedChunk.chunk().getMesh().decoration());
            }
        }
        rendererAPI.setMipmapping(true);
    }

    private void renderTransparentChunks() {
        GL11C.glDepthFunc(GL11C.GL_LESS);
        GL11C.glDepthMask(false);
        GL11C.glEnable(GL11C.GL_BLEND);
        GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
        for (int i = transparentRenderedChunksInFrustum.size() - 1; i >= 0; i--) {
            PositionedChunk positionedChunk = transparentRenderedChunksInFrustum.get(i);
            if (positionedChunk.chunk().getMesh().transparent().vao() > 0 && positionedChunk.chunk().getMesh().transparent().indexCount() > 0) {
                Position3D position3D = positionedChunk.pos();
                rendererAPI.setShaderIVec3("chunkPosition", position3D.x(), position3D.y(), position3D.z());
                rendererAPI.setShaderInt("chunkScale", 1 << positionedChunk.chunk().getMesh().lod());
                rendererAPI.render(positionedChunk.chunk().getMesh().transparent());
            }
        }
    }
}