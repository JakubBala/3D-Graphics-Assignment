package engine.scene;

import java.util.List;
import java.util.ArrayList;
import com.jogamp.opengl.GL3;

import engine.components.Light;
import engine.gmaths.*;

// TODO: Must have a MainCamera that is available to all GameObjects

public class Scene {
    private String name;
    private List<GameObject> gameObjects = new ArrayList<>();

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

    // public List<Light> getActiveLights(){
    //     List<Light> lights = new ArrayList<>();
    //     collectLights(gameObjects, lights);
    //     return lights;
    // }

    // private void collectLights(List<GameObject> gameObjects, List<Light> lights) {
    //     for (GameObject gameObj : gameObjects) {
    //         Light light = gameObj.getComponent(Light.class);
    //         if (light != null && light.isEnabled()) {
    //             lights.add(light);
    //         }
    //         collectLights(gameObj.getChildren(), lights);
    //     }
    // }

    // public void render(GL3 gl, Mat4 view, Mat4 proj, Vec3 cameraPos) {

    //     for (GameObject gameObject : gameObjects){
    //         gameObject.render(gl, view, proj, cameraPos, getActiveLights());
    //     }
    // }

    public void render(GL3 gl, Mat4 view, Mat4 proj,
        Vec3 cameraPos, 
        Vec3 lightPosition, Vec3 ambient, Vec3 diffuse, Vec3 specular) {
        for (GameObject gameObject : gameObjects){
            gameObject.render(gl, view, proj, cameraPos, lightPosition, ambient, diffuse, specular);
        }
    }

    
}