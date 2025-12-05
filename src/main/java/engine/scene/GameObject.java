package engine.scene;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;

import engine.components.Behaviour;
import engine.components.Light;
import engine.components.Transform;
import engine.components.core.Component;
import engine.components.core.Renderable;
import engine.components.core.Updatable;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;

public class GameObject {

    // SERIALIZED
    private String id; 
    private String name;
    private Transform transform;
    private List<Component> components;
    private List<GameObject> children = new ArrayList<>();

    // INSTANCED
    private GameObject parent;
    private Scene scene; //owner scene

    // TODO: Initialize GameObject with components already set up

    public GameObject(){ 
        this.transform = new Transform();
        this.components = new ArrayList<>();
        this.transform.setGameObject(this);
        components.add(transform);
    }

    public void Start() {
        for (Component c : components) {
            if (c instanceof Behaviour) {
                ((Behaviour) c).Start();
            }
        }
        for (GameObject child : children) {
            child.Start();
        }
    }

    public void Update() {
        for (Component c : components) {
            if (c instanceof Updatable) {
                ((Updatable) c).Update();
            }
        }
        for (GameObject child : children) {
            child.Update();
        }
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        // Recursively set scene for all children
        for (GameObject child : children) {
            child.setScene(scene);
        }
    }

    public Scene getScene() {
        return scene;
    }

    // Add a child
    public void addChild(GameObject child) {
        if (child.parent != null) {
            child.parent.removeChild(child);  // Remove from old parent
        }
        
        children.add(child);
        child.parent = this;
        child.transform.markDirty();  // Child's world space changed
    } 

    // Remove a child
    public void removeChild(GameObject child) {
        if (children.remove(child)) {
            child.parent = null;
            child.transform.markDirty();
        }
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

    public List<Component> getComponents() {
        return components;
    }

    public void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition, List<Light> lights) {

        // Render this GameObject's components
        for (Component c : components) {
            if (c instanceof Renderable) {
                ((Renderable) c).render(gl, view, projection, cameraPosition, lights);
            }
        }

        // Recursively render all children
        for (GameObject child : children) {
            child.render(gl, view, projection, cameraPosition, lights);
        }
    }
    
    public String getName(){ return name; }
    public Transform getTransform(){ return transform; }
    public GameObject getParent(){ return parent; }
    public List<GameObject> getChildren(){ return children; } 

    public void setName(String name){
        this.name = name;
    }
    public void setParent(GameObject parent){
        this.parent = parent;
    }
}
