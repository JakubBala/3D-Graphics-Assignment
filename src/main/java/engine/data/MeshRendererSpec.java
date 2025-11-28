package engine.data;
import engine.components.MeshRenderer;
import engine.components.core.Component;
import engine.rendering.MeshLibrary;
import engine.loaders.MaterialLoader;
import engine.scene.GameObject;
import com.jogamp.opengl.GL3;

public class MeshRendererSpec extends ComponentSpec {
    // Fields map directly to YAML
    public String mesh;      
    public String material;  
    
    @Override  
    public Component createComponent(GameObject gameObject, GL3 gl) {
        // Load resources
        var loadedMesh = MeshLibrary.Load(mesh, gl);
        var loadedMaterial = MaterialLoader.Load(gl, material);
        
        // Create the actual runtime Component
        MeshRenderer component = new MeshRenderer(loadedMesh, loadedMaterial);
        
        System.out.println("[MeshRendererSpec]: Created MeshRenderer component");
        return component;  // GameObjectLoader will add it
    }
}
