package engine.loaders;

import java.util.Map;
import java.util.List;

import com.jogamp.opengl.GL3;

import engine.scene.GameObject;
import engine.data.GameObjectSpec;
import engine.data.TransformSpec;

import engine.components.Transform;
import engine.components.MeshRenderer;
import engine.loaders.MaterialLoader;
import engine.rendering.Material;
import engine.rendering.MeshLibrary;
import engine.gmaths.*;

public class GameObjectLoader {

    public static GameObject Load(GameObjectSpec spec, GL3 gl) {

        GameObject go = new GameObject();

        go.setName(spec.name);
        System.out.println("[GameObjectLoader]: Instantiated GameObject: " + go.getName());
        if (spec.components != null) {
            for (Map<String, Object> c : spec.components) {
                loadComponent(go, c, gl);
            }
        }

        return go;
    }

    private static void loadComponent(GameObject go, Map<String, Object> spec, GL3 gl) {

        String type = (String) spec.get("type");
        if (type == null) return;

        switch (type) {

            case "Transform": {
                Transform transform = go.getTransform();

                List<?> pos = (List<?>) spec.get("local_position");
                if (pos != null && pos.size() == 3)
                    transform.SetLocalPosition(
                        ((Number)pos.get(0)).floatValue(),
                        ((Number)pos.get(1)).floatValue(),
                        ((Number)pos.get(2)).floatValue()
                    );

                List<?> rot = (List<?>) spec.get("local_rotation");
                if (rot != null && rot.size() == 3)
                    transform.SetLocalRotation(
                        ((Number)rot.get(0)).floatValue(),
                        ((Number)rot.get(1)).floatValue(),
                        ((Number)rot.get(2)).floatValue()
                    );

                List<?> scale = (List<?>) spec.get("local_scale");
                if (scale != null && scale.size() == 3)
                    transform.SetLocalScale(
                        ((Number)scale.get(0)).floatValue(),
                        ((Number)scale.get(1)).floatValue(),
                        ((Number)scale.get(2)).floatValue()
                    );

                System.out.println("[GameObjectLoader]: Loaded Transform Component");
                break;
            }


            case "MeshRenderer": {
                String meshPath = (String) spec.get("mesh");
                String materialPath = (String) spec.get("material");

                var mesh = MeshLibrary.Load(meshPath, gl);
                var material = MaterialLoader.Load(gl, materialPath);

                go.addComponent(new MeshRenderer(mesh, material));
                System.out.println("[GameObjectLoader]: Loaded MeshRenderer Component");
                break;
            }

            // other component loaders can be added later:
            //
            // case "Light": ...
            // case "Camera": ...
            // case "Script": ...
        }
    }
}