package engine.scene;

import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import engine.components.Light;
import engine.components.core.Component;
import engine.components.core.Renderable;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.rendering.Material;
import engine.rendering.Mesh;

public class Skybox{
    private Material skyboxMaterial;
    private Mesh skyboxMesh;

    public Skybox(GL3 gl, Material skyboxMaterial) {
        this.skyboxMaterial = skyboxMaterial;
        this.skyboxMesh = createSkyboxCube(gl);
    }
    private Mesh createSkyboxCube(GL3 gl) {
        // Skybox cube vertices (just positions, padded with dummy normals and UVs)
        float[] vertices = {
            // Positions        // Normals (dummy)  // TexCoords (dummy)
            // Back face
            -1.0f, -1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f, -1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f,  1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f,  1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            // Front face
            -1.0f, -1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f, -1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f,  1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f,  1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            // Left face
            -1.0f, -1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f, -1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f,  1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f,  1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            // Right face
             1.0f, -1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f, -1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f,  1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f,  1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            // Bottom face
            -1.0f, -1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f, -1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f, -1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f, -1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            // Top face
            -1.0f,  1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f,  1.0f, -1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
             1.0f,  1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f,
            -1.0f,  1.0f,  1.0f,  0.0f, 0.0f, 0.0f,  0.0f, 0.0f
        };
        
        int[] indices = {
            0, 1, 2, 2, 3, 0,       // Back
            4, 5, 6, 6, 7, 4,       // Front
            8, 9, 10, 10, 11, 8,    // Left
            12, 13, 14, 14, 15, 12, // Right
            16, 17, 18, 18, 19, 16, // Bottom
            20, 21, 22, 22, 23, 20  // Top
        };
        
        return new Mesh(gl, vertices, indices);
    }

    public void render(GL3 gl, Mat4 viewMatrix, Mat4 projectionMatrix) {
        // Disable face culling so we can see inside the cube
        gl.glDisable(GL3.GL_CULL_FACE);
        
        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glDepthMask(false); // Don't write to depth buffer
        
        // Remove translation from view matrix
        Mat4 viewNoTranslation = removeTranslation(viewMatrix);
        
        // Use skybox shader
        skyboxMaterial.useShader(gl);
        skyboxMaterial.apply(gl);
        
        // Set MVP matrix
        Mat4 mvp = Mat4.multiply(projectionMatrix, viewNoTranslation);
        skyboxMaterial.getShader().setFloatArray(gl, "mvpMatrix", mvp.toFloatArrayForGLSL());
        
        // Render
        skyboxMesh.render(gl);
        
        // Restore defaults
        gl.glDepthMask(true);
        gl.glDepthFunc(GL3.GL_LESS);
        gl.glEnable(GL3.GL_CULL_FACE); // Re-enable face culling for normal objects
    }

    public void SetTimeOfDay(float timeOfDay){
        skyboxMaterial.setUniform("timeOfDay", timeOfDay);
    }
    
    private Mat4 removeTranslation(Mat4 view) {
        // Create a copy and zero out the translation column (4th column = indices 3, 7, 11)
        Mat4 result = new Mat4(view);
        result.set(0, 3, 0.0f);
        result.set(1, 3, 0.0f);
        result.set(2, 3, 0.0f);
        return result;
    }
    
    public void dispose(GL3 gl) {
        if (skyboxMesh != null) {
            skyboxMesh.dispose(gl);
        }
    }
}
