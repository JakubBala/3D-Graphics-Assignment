package engine.components;

import com.jogamp.opengl.GL3;

import engine.components.core.Component;
import engine.components.core.Renderable;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.rendering.Material;
import engine.rendering.Mesh;

public class MeshRenderer extends Component implements Renderable{
    private Mesh mesh;
    private Material material;

    public MeshRenderer(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
    }

    @Override
    public void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition, 
        Vec3 lightPosition, Vec3 lightAmbient, Vec3 lightDiffuse, Vec3 lightSpecular
    ) {
        Mat4 model = gameObject.getTransform().getWorldMatrix();
        material.useShader(gl);
        material.setTransformUniforms(gl, model, view, projection, cameraPosition);
        material.setLightUniforms(gl, lightPosition, lightAmbient, lightDiffuse, lightSpecular);
        material.apply(gl);
        mesh.render(gl);
    }
}
