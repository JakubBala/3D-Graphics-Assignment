package engine.data;

import com.jogamp.opengl.GL3;

import engine.components.core.Component;
import engine.scene.GameObject;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public abstract class ComponentSpec {
    public String type;  // Still needed for YAML deserialization routing
    public String id;    // Optional ID for referencing
    
    /**
     * Factory method: each spec knows how to create its component
     */
    public abstract Component createComponent(GameObject gameObject, GL3 gl);
}
