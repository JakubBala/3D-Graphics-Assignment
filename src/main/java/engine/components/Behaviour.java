package engine.components;

import java.util.Map;

import engine.components.core.Component;
import engine.components.core.Updatable;
import engine.scene.GameObject;
import engine.scene.Scene;
/* I declare that this code is my own work with the help of StackOverflow and Claude*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

import java.lang.reflect.Field;

public class Behaviour extends Component implements Updatable {
    private Map<String, String> pendingReferences;

    public void Awake(){
        // called before Start/Rendering
    }

    public void Start() {
        // Called when the component is first initialized
    }

    @Override
    public void Update() {
        // Called every frame with the time since last frame
    }
    
    // Called by BehaviourSpec to store refs before scene is fully loaded
    public void setPendingReferences(Map<String, String> refs) {
        this.pendingReferences = refs;
    }


    // Called by Scene after all objects are loaded
    public void resolveReferences() {
        if (pendingReferences == null || pendingReferences.isEmpty()) {
            return;
        }
        
        Scene scene = gameObject.getScene();
        if (scene == null) {
            System.err.println("[" + getGameObject().getName() + "][Behaviour]: Cannot resolve references - GameObject not in scene");
            return;
        }
        
        for (Map.Entry<String, String> entry : pendingReferences.entrySet()) {
            String fieldName = entry.getKey();
            String refId = entry.getValue();
            
            try {
                Field field = this.getClass().getField(fieldName);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                
                Object resolved = null;
                
                // Check if field is a GameObject
                if (fieldType == GameObject.class) {
                    resolved = scene.findGameObjectById(refId);
                    if (resolved == null) {
                        System.err.println("[" + getGameObject().getName() + "][Behaviour]: GameObject with ID '" + refId + "' not found");
                    }
                }
                // Check if field is a Component type
                else if (Component.class.isAssignableFrom(fieldType)) {
                    Component component = scene.findComponentById(refId);
                    if (component != null && fieldType.isInstance(component)) {
                        resolved = component;
                    } else {
                        System.err.println("[" + getGameObject().getName() + "][Behaviour]: Component with ID '" + refId + "' not found or wrong type");
                    }
                }
                
                if (resolved != null) {
                    field.set(this, resolved);
                    System.out.println("[" + getGameObject().getName() + "][Behaviour]: Resolved reference " + fieldName + " -> " + refId);
                }
                
            } catch (Exception e) {
                System.err.println("[" + getGameObject().getName() + "][Behaviour]: Failed to resolve reference " + fieldName + ": " + e.getMessage());
            }
        }
        
        pendingReferences = null; // Clear after resolving
    }
}
