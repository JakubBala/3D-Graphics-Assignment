package engine.data;

import com.jogamp.opengl.GL3;

import engine.components.core.Component;
import engine.scene.GameObject;

public abstract class ComponentSpec {
    public String type;  // Still needed for YAML deserialization routing
    
    /**
     * Factory method: each spec knows how to create its component
     */
    public abstract Component createComponent(GameObject gameObject, GL3 gl);
}
