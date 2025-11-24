package engine.components.core;

import engine.scene.GameObject;

public abstract class Component {
    protected GameObject gameObject;
    public void setGameObject(GameObject gameObject){
        this.gameObject = gameObject;
    }

    public GameObject getGameObject(){
        return gameObject;
    }
}