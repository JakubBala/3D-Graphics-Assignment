package engine.scene;

import java.util.List;
import java.util.ArrayList;
import com.jogamp.opengl.GL3;

import engine.components.Camera;
import engine.components.Light;
import engine.gmaths.*;

// TODO: Must have a MainCamera that is available to all GameObjects

public class Scene {
    private String name;
    private List<GameObject> gameObjects = new ArrayList<>();
    private Skybox skybox;
    private Camera mainCamera;

    public Scene(String name){
        this.name = name;
    }

    public void AddGameObject(GameObject go) {
        gameObjects.add(go);
    }

    public void Update() {
        for (GameObject go : gameObjects)
            go.update();
    }

    public String getName(){
        return name;
    }

    public void SetSkybox(Skybox skybox){
        this.skybox = skybox;
    }

    public void render(GL3 gl) {
        // 1. Render skybox FIRST (if it exists)
        if (skybox != null) {
            skybox.render(gl, mainCamera.getViewMatrix(), mainCamera.getPerspectiveMatrix());
        }

        Mat4 viewMatrix = mainCamera.getViewMatrix();
        Mat4 projectionMatrix = mainCamera.getPerspectiveMatrix();
        Vec3 camPos = mainCamera.getGameObject().getTransform().GetWorldPosition();

        // 2. Render all GameObjects
        for (GameObject gameObject : gameObjects){
            gameObject.render(gl, viewMatrix, projectionMatrix, camPos, getActiveLights());
        }
    }

    public List<Light> getActiveLights(){
        List<Light> lights = new ArrayList<>();
        collectLights(gameObjects, lights);
        return lights;
    }

    private void collectLights(List<GameObject> gameObjects, List<Light> lights) {
        for (GameObject gameObj : gameObjects) {
            Light light = gameObj.getComponent(Light.class);
            if (light != null && light.isEnabled()) {
                lights.add(light);
            }
            collectLights(gameObj.getChildren(), lights);
        }
    }

    public void findAndSetMainCamera() {
        for (GameObject gameObject : gameObjects) {
            Camera camera = gameObject.getComponent(Camera.class);
            if (camera != null && camera.isMainCamera()) {
                mainCamera = camera;
                return;
            }
        }
        System.err.println("Warning: No main camera found in scene '" + name + "'");
        mainCamera = null; // No main camera found
    }

    public Camera getMainCameraInstance() {
        if(mainCamera == null) {
            findAndSetMainCamera();
        }
        return mainCamera;
    } 

    public void keyboardInput(Camera.Movement movement) {
        mainCamera.move(movement);
    }

    public void mouseInput(float dx, float dy) {
        mainCamera.updateYawPitch(dx, dy);
    }

    public void windowResized(float width, float height) {
        if(mainCamera == null) {
            findAndSetMainCamera();
        }
        if(mainCamera == null) return; // No main camera to control
        float aspect = width / height;
        mainCamera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect, 0.1f, 300f));

    }
}