package engine.components.core;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

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