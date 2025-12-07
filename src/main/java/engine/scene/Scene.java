package engine.scene;

import java.util.List;
import java.util.ArrayList;
import com.jogamp.opengl.GL3;

import engine.components.Behaviour;
import engine.components.Camera;
import engine.components.Light;
import engine.components.MeshRenderer;
import engine.components.Transform;
import engine.components.core.Component;
import engine.debug.BezierVisualizer;
import engine.debug.DebugAxes;
import engine.gmaths.*;
import engine.math.BezierCurve;
import engine.math.BezierPath;

// TODO: Must have a MainCamera that is available to all GameObjects

public class Scene {
    private String name;
    private List<GameObject> gameObjects = new ArrayList<>();
    private Skybox skybox;
    private Camera mainCamera;

    private DebugAxes debugAxesRenderer;

    private GL3 glContext;
    public void passGLcontext(GL3 gl){
        glContext = gl;
    }
    public GL3 getGLcontext(){
        return glContext;
    }

    public Scene(String name){
        this.name = name;
    }

    public void AddGameObject(GameObject go) {
        gameObjects.add(go);
    }

    public void Start() {
        for (GameObject go : gameObjects)
            go.Start();
    }

    public void Update() {
        for (GameObject go : gameObjects)
            go.Update();
    }

    public String getName(){
        return name;
    }

    public void SetSkybox(Skybox skybox){
        this.skybox = skybox;
    }

    public Skybox GetSkybox(){
        return skybox;
    }

    public void initDebugRenderers(GL3 gl) {
        this.debugAxesRenderer = new DebugAxes(gl, 0.25f);
    }

    public void render(GL3 gl) {
        // 1. Render skybox first (if it exists)
        if (skybox != null) {
            skybox.render(gl, mainCamera.getViewMatrix(), mainCamera.getPerspectiveMatrix());
        }

        Mat4 viewMatrix = mainCamera.getViewMatrix();
        Mat4 perspectiveMatrix = mainCamera.getPerspectiveMatrix();
        Vec3 camPos = mainCamera.getGameObject().getTransform().GetWorldPosition();

        // 2. Render all GameObjects
        List<GameObject> opaque = new ArrayList<>();
        List<GameObject> transparent = new ArrayList<>();

        for (GameObject obj : gameObjects) {
            MeshRenderer mr = obj.getComponent(MeshRenderer.class);
            if (mr == null){
                // we will still need to render empties
                // because of their children
                opaque.add(obj);
                continue;
            } 

            if (mr.getMaterial().isTransparent())
                transparent.add(obj);
            else
                opaque.add(obj);
        }

        // --- PASS 1: OPAQUE OBJECTS ---
        for (GameObject obj : opaque) {
            obj.render(gl, viewMatrix, perspectiveMatrix, camPos, getActiveLights());
        }

        // --- PASS 2: TRANSPARENT OBJECTS (sorted back->front) ---
        transparent.sort((a, b) -> {
            float da = a.getTransform().GetWorldPosition().distance(camPos);
            float db = b.getTransform().GetWorldPosition().distance(camPos);
            return Float.compare(db, da);
        });

        for (GameObject obj : transparent) {
            obj.render(gl, viewMatrix, perspectiveMatrix, camPos, getActiveLights());
        }

        // 3. Render debug axes for Transforms that have it enabled
        Vec3 cameraPos = mainCamera.getGameObject().getTransform().GetWorldPosition();
        renderDebugAxes(gl, viewMatrix, perspectiveMatrix, cameraPos);
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
        mainCamera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect, 0.1f, 10000f));

    }

    /**
     * Find a GameObject by its ID
     */
    public GameObject findGameObjectById(String id) {
        if (id == null) return null;
        
        for (GameObject go : gameObjects) {
            GameObject found = findGameObjectByIdRecursive(go, id);
            if (found != null) return found;
        }
        return null;
    }
    
    private GameObject findGameObjectByIdRecursive(GameObject go, String id) {
        if (id.equals(go.getId())) {
            return go;
        }
        
        for (GameObject child : go.getChildren()) {
            GameObject found = findGameObjectByIdRecursive(child, id);
            if (found != null) return found;
        }
        
        return null;
    }

    /**
     * Find a Component by its ID (searches all GameObjects and their components)
     */
    public Component findComponentById(String id) {
        if (id == null) return null;
        
        for (GameObject go : gameObjects) {
            Component found = findComponentByIdRecursive(go, id);
            if (found != null) return found;
        }
        return null;
    }
    
    private Component findComponentByIdRecursive(GameObject go, String id) {
        // Check all components on this GameObject
        for (Component c : go.getComponents()) {
            if (id.equals(c.getId())) {
                return c;
            }
        }
        
        // Check children
        for (GameObject child : go.getChildren()) {
            Component found = findComponentByIdRecursive(child, id);
            if (found != null) return found;
        }
        
        return null;
    }

    public void resolveAllReferences() {
        for (GameObject go : gameObjects) {
            resolveReferencesRecursive(go);
        }
    }

    private void resolveReferencesRecursive(GameObject go) {
        for (Component c : go.getComponents()) {
            if (c instanceof Behaviour) {
                ((Behaviour) c).resolveReferences();
            }
        }
        for (GameObject child : go.getChildren()) {
            resolveReferencesRecursive(child);
        }
    }

    private void renderDebugAxes(GL3 gl, Mat4 view, Mat4 perspective, Vec3 cameraPos) {
        for (GameObject go : gameObjects) {
            renderDebugAxesRecursive(gl, go, view, perspective, cameraPos);
        }
    }
    
    private void renderDebugAxesRecursive(GL3 gl, GameObject go, Mat4 view, Mat4 perspective, Vec3 cameraPos) {
        Transform transform = go.getTransform();
        if (transform.isDebugAxesEnabled()) {
            debugAxesRenderer.render(gl, transform.getWorldMatrix(), view, perspective, cameraPos, true);
        }
        
        // Recurse to children
        for (GameObject child : go.getChildren()) {
            renderDebugAxesRecursive(gl, child, view, perspective, cameraPos);
        }
    }
}