package engine.data;

import com.jogamp.opengl.GL3;

import engine.components.Camera;
import engine.components.core.Component;
import engine.scene.GameObject;

public class CameraSpec extends ComponentSpec {
    public boolean movementEnabled = true;
    public boolean isMainCamera = false;

    @Override
    public Component createComponent(GameObject gameObject, GL3 gl) {
        Camera camera = new Camera();
        camera.setMovementEnabled(movementEnabled);
        camera.setMainCamera(isMainCamera);
        return camera;
    }
}
