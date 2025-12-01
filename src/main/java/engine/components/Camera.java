package engine.components;

import engine.components.core.Component;
import engine.gmaths.*;
import java.awt.event.*;
import com.jogamp.opengl.awt.GLCanvas;

public class Camera extends Component{
     // --- Movement Types ---
    public enum Movement {
        NONE,
        FORWARD, BACK, LEFT, RIGHT, UP, DOWN,
        FAST_FORWARD, FAST_BACK, FAST_LEFT, FAST_RIGHT, FAST_UP, FAST_DOWN;

        public boolean isFast() {
            return this == FAST_FORWARD || this == FAST_BACK ||
                   this == FAST_LEFT || this == FAST_RIGHT ||
                   this == FAST_UP || this == FAST_DOWN;
        }
    }

    // --- Settings ---

    private float moveSpeed = 0.2f;
    private float fastMultiplier = 5f;
    private float mouseSensitivity = 30f;

    private boolean movementEnabled = true;
    private boolean isMainCamera = false;

    private Mat4 perspective;

    public boolean isMovementEnabled() {
        return movementEnabled;
    }

    public void setMovementEnabled(boolean enabled) {
        this.movementEnabled = enabled;
    }

    public boolean isMainCamera() {
        return isMainCamera;
    }

    public void setMainCamera(boolean isMain) {
        this.isMainCamera = isMain;
    } 

    // --- Constructor ---
    public Camera() {
    }

    // --- Matrices --- 
    public void setPerspectiveMatrix(Mat4 p) {
        this.perspective = p;
    }

    public Mat4 getPerspectiveMatrix() {
        return perspective;
    }

    public Mat4 getViewMatrix() {
        Transform t = getGameObject().getTransform();

        Vec3 pos = t.GetWorldPosition();
        Vec3 forward = t.GetForward();
        Vec3 up = t.GetUp();

        return Mat4Transform.lookAt(pos, Vec3.add(pos, forward), up);
    }

    // -------------------------------------------------------------------------
    //   MOVEMENT
    // -------------------------------------------------------------------------

    // TODO: The current way always makes it move with local amount, consider making it a world speed.
    public void move(Movement m) {
        if (!movementEnabled || m == Movement.NONE)
            return;

        Transform t = getGameObject().getTransform();

        float base = moveSpeed;
        float speed = (m.isFast() ? base * fastMultiplier : base);
        Vec3 translation = new Vec3(0,0,0);

        switch (m) {
            case FORWARD:
            case FAST_FORWARD:
                translation = Vec3.multiply(t.GetForward(), speed);
                break;

            case BACK:
            case FAST_BACK:
                translation = Vec3.multiply(t.GetForward(), -speed);
                break;

            case LEFT:
            case FAST_LEFT:
                translation = Vec3.multiply(t.GetRight(), -speed);
                break;

            case RIGHT:
            case FAST_RIGHT:
                translation = Vec3.multiply(t.GetRight(), speed);
                break;

            case UP:
            case FAST_UP:
                translation = Vec3.multiply(t.GetUp(), speed);
                break;

            case DOWN:
            case FAST_DOWN:
                translation = Vec3.multiply(t.GetUp(), -speed);
                break;

            default: break;
        }

        t.Translate(translation);
    }

    // -------------------------------------------------------------------------
    //   MOUSE LOOK — Converts delta yaw/ delta pitch to Transform rotation
    // -------------------------------------------------------------------------
    public void updateYawPitch(float deltaYaw, float deltaPitch) {
        if (!movementEnabled)
            return;

        Transform t = getGameObject().getTransform();
        Vec3 localRot = t.GetRotation(); // x = pitch, y = yaw, z = roll

        // Update yaw and pitch values
        float newYaw = localRot.y + deltaYaw * mouseSensitivity;
        float newPitch = localRot.x + deltaPitch * mouseSensitivity;

        System.err.println("Pitch: " + newPitch + ", Yaw: " + newYaw);

        // Clamp vertical rotation (in degrees)
        newPitch = Math.max(-89f, Math.min(89f, newPitch));

        // Apply updated rotation back to the Transform
        // The key is to ALWAYS keep roll at 0 for FPS camera
        // Transform's rotation order (Z*Y*X) should handle this correctly
        // when roll is 0: it becomes (Y*X) which is yaw-then-pitch
        t.SetLocalRotation(newPitch, newYaw, 0f);

    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------
    public float getYaw() {
        Transform t = getGameObject().getTransform();
        if (t == null) return -90f;
        return t.GetRotation().y;
    }

    public float getPitch() {
        Transform t = getGameObject().getTransform();
        if (t == null) return 0f;
        return t.GetRotation().x;
    }

    public float getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(float s) { moveSpeed = s; }

    public float getMouseSensitivity() { return mouseSensitivity; }
    public void setMouseSensitivity(float s) { mouseSensitivity = s; }
}
