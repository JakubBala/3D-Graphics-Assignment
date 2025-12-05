package engine.components.core;

import engine.scene.GameObject;

public abstract class Component {
    protected GameObject gameObject;
    private String id; // required for components that will be referenced
    public void setGameObject(GameObject gameObject){
        this.gameObject = gameObject;
    }

    public GameObject getGameObject(){
        return gameObject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}