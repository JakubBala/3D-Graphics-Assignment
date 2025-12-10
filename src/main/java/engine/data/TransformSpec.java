package engine.data;

import java.util.List;

import com.jogamp.opengl.GL3;

import engine.components.core.Component;
import engine.data.ComponentSpec;
import engine.scene.GameObject;
import engine.components.Transform;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class TransformSpec extends ComponentSpec {
    // Fields that match YAML structure
    public float[] position;  // [x, y, z]
    public float[] rotation;  // [x, y, z]
    public float[] scale;     // [x, y, z]
    public boolean debugAxes;

    @Override
    public Component createComponent(GameObject gameObject, GL3 gl) {
        // Transform already exists on GameObject, so just configure it
        Transform transform = gameObject.getTransform();
        
        if (position != null && position.length == 3) {
            transform.SetLocalPosition(position[0], position[1], position[2]);
        }
        
        if (rotation != null && rotation.length == 3) {
            transform.SetLocalRotation(rotation[0], rotation[1], rotation[2]);
        }
        
        if (scale != null && scale.length == 3) {
            transform.SetLocalScale(scale[0], scale[1], scale[2]);
        }

        if(debugAxes) {
            transform.enableDebugAxes();
        }
        
        System.out.println("[TransformSpec]: Configured transform");
        return transform;  // Return existing component
    }
}