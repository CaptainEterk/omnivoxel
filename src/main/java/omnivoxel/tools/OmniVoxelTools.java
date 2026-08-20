package omnivoxel.tools;

import omnivoxel.client.game.graphics.api.opengl.mesh.Quad;
import omnivoxel.client.game.graphics.api.opengl.shader.ShaderProgram;
import omnivoxel.client.game.graphics.api.opengl.shader.ShaderProgramHandler;
import omnivoxel.client.game.graphics.api.opengl.window.Window;
import omnivoxel.client.game.graphics.api.opengl.window.WindowFactory;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class OmniVoxelTools {

    private final Queue<Consumer<Window>> contextTasks;

    private final Quad quad;
    private final NoiseTexture noiseTexture;
    private final Noise3D noise;
    private final SplineMapper splineMapper;
    private final SplineRenderer splineRenderer;

    private ShaderProgram shaderProgram;
    private ShaderProgram splineShader;

    private Window window;

    private double cameraX = 0.0;
    private double cameraY = 0.0;
    private double noiseZ = 0.0;

    /*
     * Pixels per world unit.
     */
    private double zoom = 100.0;

    /*
     * Noise coordinate multiplier.
     */
    private double noiseScale = 0.01;

    private boolean dirty = true;

    private double lastMouseX;
    private double lastMouseY;

    private boolean panning = false;
    private int selectedSplinePoint = -1;

    private boolean previousLeftButton = false;
    private boolean previousRightButton = false;

    /*
     * Bottom spline editor.
     */
    private static final float SPLINE_HEIGHT = 160.0f;
    private static final float SPLINE_MARGIN = 20.0f;

    private float splineX;
    private float splineY;
    private float splineWidth;
    private float splineHeight;

    public OmniVoxelTools() {
        contextTasks =
                new LinkedBlockingDeque<>();

        quad = new Quad(
                0.0f,
                0.0f,
                500.0f,
                500.0f
        );

        noise = new Noise3D(
                new double[] {
                        1.0
                },
                -2,
                12345L
        );

        splineMapper =
                new SplineMapper();

        splineRenderer =
                new SplineRenderer();

        noiseTexture =
                new NoiseTexture(512, 512);
    }

    public static void main(String[] args)
            throws IOException {

        new OmniVoxelTools().start();
    }

    public void start() throws IOException {
        window = WindowFactory.createWindow(
                900,
                700,
                "OmniVoxel Tools v0.0-alpha",
                contextTasks,
                true
        );

        GLFW.glfwSetScrollCallback(
                window.window(),
                (handle, xOffset, yOffset) -> {
                    double[] mouseX = new double[1];
                    double[] mouseY = new double[1];

                    GLFW.glfwGetCursorPos(
                            handle,
                            mouseX,
                            mouseY
                    );

                    onScroll(
                            mouseX[0],
                            mouseY[0],
                            yOffset
                    );
                }
        );

        ShaderProgramHandler handler =
                new ShaderProgramHandler();

        handler.addShaderProgram(
                "tools",
                Map.of(
                        "assets/shaders/tools.vert",
                        GL20.GL_VERTEX_SHADER,

                        "assets/shaders/tools.frag",
                        GL20.GL_FRAGMENT_SHADER
                )
        );

        handler.addShaderProgram(
                "spline",
                Map.of(
                        "assets/shaders/spline.vert",
                        GL20.GL_VERTEX_SHADER,

                        "assets/shaders/spline.frag",
                        GL20.GL_FRAGMENT_SHADER
                )
        );

        shaderProgram =
                handler.getShaderProgram("tools");

        splineShader =
                handler.getShaderProgram("spline");

        window.addResizingCallback(w -> {
            updateProjection();

            updateLayout();

            dirty = true;
        });

        window.init(900, 700);

        quad.init();

        noiseTexture.init();

        splineRenderer.init(2048);

        updateLayout();
        updateProjection();

        window.show();

        while (!window.shouldClose()) {
            GL11C.glClearColor(
                    0.04f,
                    0.04f,
                    0.04f,
                    1.0f
            );

            GL11C.glClear(
                    GL11C.GL_COLOR_BUFFER_BIT
                            | GL11C.GL_DEPTH_BUFFER_BIT
            );

            Consumer<Window> task;

            while ((task = contextTasks.poll()) != null) {
                task.accept(window);
            }

            processInput();

            if (dirty) {
                regenerateNoise();
                dirty = false;
            }

            frame();

            GLFW.glfwSwapBuffers(
                    window.window()
            );

            GLFW.glfwPollEvents();
        }

        noiseTexture.cleanup();
        splineRenderer.cleanup();
        quad.cleanup();

        GLFW.glfwDestroyWindow(
                window.window()
        );

        GLFW.glfwTerminate();
    }

    private void updateProjection() {
        Matrix4f projection =
                new Matrix4f().ortho(
                        0.0f,
                        window.getWidth(),

                        window.getHeight(),
                        0.0f,

                        -1.0f,
                        1.0f
                );

        shaderProgram.bind();

        shaderProgram.setUniform(
                "projection",
                projection
        );

        splineShader.bind();

        splineShader.setUniform(
                "projection",
                projection
        );
    }

    private void updateLayout() {
        float width =
                window.getWidth();

        float height =
                window.getHeight();

        float viewportHeight =
                Math.max(
                        1.0f,
                        height - SPLINE_HEIGHT
                );

        quad.setBounds(
                0.0f,
                0.0f,
                width,
                viewportHeight
        );

        splineX =
                SPLINE_MARGIN;

        splineY =
                viewportHeight
                        + 20.0f;

        splineWidth =
                width
                        - SPLINE_MARGIN * 2.0f;

        splineHeight =
                SPLINE_HEIGHT
                        - 40.0f;
    }

    private void regenerateNoise() {
        double zoomForTexture =
                zoom;

        noiseTexture.update(
                noise,
                splineMapper,
                cameraX,
                cameraY,
                noiseZ,
                zoomForTexture,
                noiseScale
        );
    }

    private void frame() {
        GL11C.glDisable(
                GL11C.GL_DEPTH_TEST
        );

        GL11C.glDisable(
                GL11C.GL_CULL_FACE
        );

        GL11C.glDisable(
                GL11C.GL_SCISSOR_TEST
        );

        GL11C.glViewport(
                0,
                0,
                window.getWidth(),
                window.getHeight()
        );

        /*
         * Noise viewport.
         */
        shaderProgram.bind();

        noiseTexture.bind(0);

        shaderProgram.setUniform(
                "noiseTexture",
                0
        );

        quad.render();

        /*
         * Spline editor.
         */
        splineShader.bind();

        splineShader.setUniform(
                "projection",
                new Matrix4f().ortho(
                        0.0f,
                        window.getWidth(),
                        window.getHeight(),
                        0.0f,
                        -1.0f,
                        1.0f
                )
        );

        splineShader.setUniform(
                "selectedPoint",
                selectedSplinePoint
        );

        splineRenderer.render(
                splineMapper,
                selectedSplinePoint,
                splineX,
                splineY,
                splineWidth,
                splineHeight,
                256
        );
    }

    private void processInput() {
        long glfwWindow =
                window.window();

        double[] mouseX =
                new double[1];

        double[] mouseY =
                new double[1];

        GLFW.glfwGetCursorPos(
                glfwWindow,
                mouseX,
                mouseY
        );

        double mx = mouseX[0];
        double my = mouseY[0];

        boolean left =
                GLFW.glfwGetMouseButton(
                        glfwWindow,
                        GLFW.GLFW_MOUSE_BUTTON_LEFT
                ) == GLFW.GLFW_PRESS;

        boolean middle =
                GLFW.glfwGetMouseButton(
                        glfwWindow,
                        GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                ) == GLFW.GLFW_PRESS;

        boolean right =
                GLFW.glfwGetMouseButton(
                        glfwWindow,
                        GLFW.GLFW_MOUSE_BUTTON_RIGHT
                ) == GLFW.GLFW_PRESS;

        /*
         * Middle mouse = pan.
         */
        if (middle && !panning) {
            panning = true;

            lastMouseX = mx;
            lastMouseY = my;
        }

        if (!middle) {
            panning = false;
        }

        if (panning) {
            double dx =
                    mx - lastMouseX;

            double dy =
                    my - lastMouseY;

            cameraX -= dx / zoom;
            cameraY -= dy / zoom;

            lastMouseX = mx;
            lastMouseY = my;

            dirty = true;
        }

        /*
         * Left mouse.
         */
        boolean leftPressed =
                left && !previousLeftButton;

        if (leftPressed) {
            if (insideSpline(mx, my)) {
                selectedSplinePoint =
                        splineMapper.findPoint(
                                mx,
                                my,
                                splineX,
                                splineY,
                                splineWidth,
                                splineHeight
                        );

                if (selectedSplinePoint == -1) {
                    double location =
                            (mx - splineX)
                                    / splineWidth;

                    double value =
                            1.0
                                    - (my - splineY)
                                    / splineHeight;

                    splineMapper.addPoint(
                            location,
                            value
                    );

                    selectedSplinePoint =
                            splineMapper.findPoint(
                                    mx,
                                    my,
                                    splineX,
                                    splineY,
                                    splineWidth,
                                    splineHeight
                            );

                    splineMapper.printPoints();

                    dirty = true;
                }
            }
        }

        if (left && selectedSplinePoint != -1) {
            double location =
                    (mx - splineX)
                            / splineWidth;

            double value =
                    1.0
                            - (my - splineY)
                            / splineHeight;

            splineMapper.movePoint(
                    selectedSplinePoint,
                    location,
                    value
            );

            dirty = true;
        }

        if (!left && previousLeftButton) {
            if (selectedSplinePoint != -1) {
                splineMapper.recalculateDerivatives();
                splineMapper.printPoints();
            }

            selectedSplinePoint = -1;
        }

        /*
         * Right mouse removes a spline point.
         */
        boolean rightPressed =
                right && !previousRightButton;

        if (rightPressed && insideSpline(mx, my)) {
            int point =
                    splineMapper.findPoint(
                            mx,
                            my,
                            splineX,
                            splineY,
                            splineWidth,
                            splineHeight
                    );

            if (point != -1) {
                splineMapper.removePoint(point);

                selectedSplinePoint = -1;

                splineMapper.printPoints();

                dirty = true;
            }
        }

        /*
         * GLFW doesn't expose accumulated scroll directly
         * through this polling method, so scroll is handled
         * by the callback below.
         */

        previousLeftButton = left;
        previousRightButton = right;
    }

    private boolean insideSpline(
            double x,
            double y
    ) {
        return x >= splineX
                && x <= splineX + splineWidth
                && y >= splineY
                && y <= splineY + splineHeight;
    }

    public void onScroll(
            double mouseX,
            double mouseY,
            double scrollY
    ) {
        if (scrollY == 0.0) {
            return;
        }

        /*
         * Don't zoom while over the spline editor.
         */
        if (insideSpline(mouseX, mouseY)) {
            return;
        }

        double beforeX =
                cameraX
                        + (mouseX - window.getWidth() * 0.5)
                        / zoom;

        double beforeY =
                cameraY
                        + (mouseY - window.getHeight() * 0.5)
                        / zoom;

        double factor =
                Math.pow(1.15, scrollY);

        zoom *= factor;

        zoom = Math.clamp(zoom,
                0.01, 500.0);

        double afterX =
                cameraX
                        + (mouseX - window.getWidth() * 0.5)
                        / zoom;

        double afterY =
                cameraY
                        + (mouseY - window.getHeight() * 0.5)
                        / zoom;

        cameraX += beforeX - afterX;
        cameraY += beforeY - afterY;

        dirty = true;
    }
}