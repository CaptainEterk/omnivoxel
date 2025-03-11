package omnivoxel.client.game.player;

import omnivoxel.client.game.entity.mob.player.PlayerEntity;
import omnivoxel.client.game.player.camera.Camera;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.util.input.OVKeyInput;
import omnivoxel.client.game.util.input.OVMouseButtonInput;
import omnivoxel.client.game.util.input.OVMouseInput;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.MovedRequest;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class PlayerController extends PlayerEntity {
    private final float speed = 0.5f;

    private final Client client;

    private final Camera camera;
    private final Settings settings;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final GameState gameState;

    private OVKeyInput keyInput;
    private OVMouseButtonInput mouseButtonInput;
    private OVMouseInput mouseInput;

    public PlayerController(Client client, Camera camera, Settings settings, BlockingQueue<Consumer<Long>> contextTasks, GameState gameState) {
        super("Test Player", new byte[0]);
        this.client = client;
        // TODO: Add settings for this in a menu
        this.camera = camera;
        this.settings = settings;
        this.contextTasks = contextTasks;
        this.gameState = gameState;
    }

    @Override
    public void tick(float deltaTime) {
        if (mouseButtonInput.isMouseLocked()) {
            float moveSpeed = speed * deltaTime * ConstantGameSettings.TARGET_FPS;

            // Rotation
            double deltaX = mouseInput.getDeltaX();
            double deltaY = mouseInput.getDeltaY();

            float pitchChange = (float) deltaY / settings.getFloatSetting("sensitivity", 50f) * deltaTime * ConstantGameSettings.TARGET_FPS;
            float yawChange = (float) deltaX / settings.getFloatSetting("sensitivity", 50f) * deltaTime * ConstantGameSettings.TARGET_FPS;
            setPitch(pitch + pitchChange);
            setYaw(yaw + yawChange);

            camera.rotateX(pitchChange);
            camera.rotateY(yawChange);

            // Movement
            float moveRelativeX = (keyInput.isKeyPressed(GLFW.GLFW_KEY_D) ? moveSpeed : 0) -
                    (keyInput.isKeyPressed(GLFW.GLFW_KEY_A) ? moveSpeed : 0);
            float moveRelativeY = (keyInput.isKeyPressed(GLFW.GLFW_KEY_SPACE) ? moveSpeed : 0) -
                    (keyInput.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) ? moveSpeed : 0);
            float moveRelativeZ = (keyInput.isKeyPressed(GLFW.GLFW_KEY_W) ? moveSpeed : 0) -
                    (keyInput.isKeyPressed(GLFW.GLFW_KEY_S) ? moveSpeed : 0);
            if (moveRelativeX != 0 || moveRelativeY != 0 || moveRelativeZ != 0) {
                setVelocityX(moveRelativeX);
                setVelocityY(moveRelativeY);
                setVelocityZ(moveRelativeZ);
            }
            if (moveRelativeX != 0 || moveRelativeY != 0 || moveRelativeZ != 0 || pitchChange != 0 || yawChange != 0) {
//                client.sendRequest(new MovedRequest(getX(), getY(), getZ(), pitch, yaw));
            }

            camera.setPosition(changingPosition.x(), changingPosition.y(), changingPosition.z());

            // Handle actions
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F1)) {
                gameState.setItem("shouldRenderWireframe", !gameState.getItem("shouldRenderWireframe", Boolean.class));
            }

            // Handle cursor escaping
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                contextTasks.add(mouseButtonInput::unlockMouse);
            }
        } else if (mouseButtonInput.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            contextTasks.add(mouseButtonInput::lockMouse);
        }
        mouseInput.clearDelta();
        super.tick(deltaTime);
    }

    public Camera getCamera() {
        return camera;
    }

    public void setKeyInput(OVKeyInput keyInput) {
        this.keyInput = keyInput;
    }

    public void setMouseButtonInput(OVMouseButtonInput mouseButtonInput) {
        this.mouseButtonInput = mouseButtonInput;
    }

    public void setMouseInput(OVMouseInput mouseInput) {
        this.mouseInput = mouseInput;
    }
}