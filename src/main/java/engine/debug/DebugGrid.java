package engine.debug;

import engine.gmaths.*;
import engine.rendering.Mesh;
import engine.rendering.Shader;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
/* I declare that this code is my own work with the help of StackOverflow and Claude*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class DebugGrid {

    private Mesh quad;
    private Shader shader;

    public DebugGrid(GL3 gl) {

        // FULLSCREEN QUAD (your Mesh requires 8 floats per vertex)
        float[] vertices = {
            // pos (3)      normal(3)   uv(2)
            -1, -1, 0,      0,0,1,      0,0,
             1, -1, 0,      0,0,1,      1,0,
             1,  1, 0,      0,0,1,      1,1,
            -1,  1, 0,      0,0,1,      0,1
        };

        int[] indices = { 0,1,2, 0,2,3 };

        quad = new Mesh(gl, vertices, indices);

        shader = new Shader(gl,
            "assets/shaders/vs_debug_grid.vert",
            "assets/shaders/fs_debug_grid.frag"
        );
    }

    public void render(GL3 gl, Mat4 view, Mat4 projection) {
        // Enable blending for transparency
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        // Disable depth writing so grid doesn't block objects
        gl.glDepthMask(false);
        
        shader.use(gl);
        shader.setMat4(gl, "view", view);
        shader.setMat4(gl, "projection", projection);
        quad.render(gl);
        
        // Restore depth writing
        gl.glDepthMask(true);
        
        // Disable blending
        gl.glDisable(GL.GL_BLEND);
    }

    public void dispose(GL3 gl) {
        quad.dispose(gl);
    }
}