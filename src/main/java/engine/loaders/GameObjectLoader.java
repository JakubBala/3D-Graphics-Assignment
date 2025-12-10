package engine.loaders;

import com.jogamp.opengl.GL3;

import engine.scene.GameObject;
import engine.data.ComponentSpec;
import engine.data.GameObjectSpec;
import engine.data.TransformSpec;
import engine.components.core.Component;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

public class GameObjectLoader {

    public static GameObject Load(GameObjectSpec gameObjSpec, GL3 gl) {

        GameObject newGameObj = new GameObject();

        newGameObj.setName(gameObjSpec.name);
        if (gameObjSpec.id != null) {
            newGameObj.setId(gameObjSpec.id);
        }

        // Load components
        System.out.println("[GameObjectLoader]: Instantiated GameObject: " + newGameObj.getName());
        if (gameObjSpec.components != null) {
            for (ComponentSpec componentSpec : gameObjSpec.components) {
                loadComponent(newGameObj, componentSpec, gl);
            }
        }

        // RECURSIVELY load children
        if (gameObjSpec.children != null) {
            for (GameObjectSpec childSpec : gameObjSpec.children) {
                GameObject child = Load(childSpec, gl);  // Recursive call
                newGameObj.addChild(child);
                System.out.println("[GameObjectLoader]: Added child '" + child.getName() + 
                    "' to parent '" + newGameObj.getName() + "'");
            }
        }

        return newGameObj;
    }

    private static void loadComponent(GameObject newGameObject, ComponentSpec componentSpec, GL3 gl) {

        // Create the component from the spec
        Component component = componentSpec.createComponent(newGameObject, gl);

        // Set component ID if provided
        if (componentSpec.id != null) {
            component.setId(componentSpec.id);
        }
        
        // Special handling for Transform (already exists on GameObject by default)
        if (componentSpec instanceof TransformSpec) {
            // Transform is already added to GameObject in constructor
            // The spec just configures it, no need to add again
            System.out.println("[GameObjectLoader]: Configured Transform component");
        } else {
            // All other components need to be added
            newGameObject.addComponent(component);
            System.out.println("[GameObjectLoader]: Added " + 
                component.getClass().getSimpleName() + " component");
        }
    }
}