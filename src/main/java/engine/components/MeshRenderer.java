package engine.components;

import java.util.List;

import com.jogamp.opengl.GL3;

import engine.components.core.Component;
import engine.components.core.Renderable;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.rendering.Material;
import engine.rendering.Mesh;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class MeshRenderer extends Component implements Renderable{
    private Mesh mesh;
    private Material material;

    public MeshRenderer(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition, List<Light> lights
    ) {
        Mat4 model = gameObject.getTransform().getWorldMatrix();
        material.useShader(gl);
        material.setTransformUniforms(gl, model, view, projection, cameraPosition);
        material.setLightsUniform(gl, lights);
        material.apply(gl);
        mesh.render(gl);
        material.restore(gl);
    }
}
