package engine.scene;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;

import engine.components.Transform;
import engine.components.core.Component;
import engine.components.core.Renderable;
import engine.components.core.Updatable;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;

public class GameObject {
    private String name;
    private Transform transform;
    private List<Component> components;

    // TODO: Initialize GameObject with components already set up

    public GameObject() {
        this.transform = new Transform();
        this.components = new ArrayList<>();
        this.transform.setGameObject(this);
        components.add(transform);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Transform getTransform() {
        return transform;
    }

    public void addComponent(Component c) {
        c.setGameObject(this);
        components.add(c);
    }

    public <T extends Component> T getComponent(Class<T> clazz) {
        for (Component c : components) {
            if (clazz.isInstance(c)) return clazz.cast(c);
        }
        return null;
    }

    public void update() {
        for (Component c : components) {
            if (c instanceof Updatable) {
                ((Updatable) c).update();
            }
        }
    }

    public void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition,
         Vec3 lightPosition, Vec3 lightAmbient, Vec3 lightDiffuse, Vec3 lightSpecular) {
        for (Component c : components) {
            if (c instanceof Renderable) {
                ((Renderable) c).render(gl, view, projection, cameraPosition, lightPosition, lightAmbient, lightDiffuse, lightSpecular);
            }
        }
    }
}
