package engine.debug;

import com.jogamp.opengl.GL3;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.rendering.Shader;
import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;

/**
 * Renders debug axes + forward direction line
 */
public class DebugAxes {
    
    private int[] axesVao = new int[1];
    private int[] axesVbo = new int[1];
    private int[] forwardVao = new int[1];
    private int[] forwardVbo = new int[1];
    private Shader shader;
    private float screenSpaceSize;
    
    public DebugAxes(GL3 gl, float screenSpaceSize) {
        this.screenSpaceSize = screenSpaceSize;
        this.shader = new Shader(gl, "assets/shaders/vs_debug_axes.vert", "assets/shaders/fs_debug_axes.frag");
        initAxisBuffers(gl);
        initForwardBuffers(gl);
    }
    
    private void initAxisBuffers(GL3 gl) {
        // Axes (X=red, Y=green, Z=blue)
        float[] vertices = {
            // X axis (red)
            0.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,
            
            // Y axis (green)
            0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,
            
            // Z axis (blue)
            0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f
        };
        
        gl.glGenVertexArrays(1, axesVao, 0);
        gl.glBindVertexArray(axesVao[0]);
        
        gl.glGenBuffers(1, axesVbo, 0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, axesVbo[0]);
        
        FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL3.GL_STATIC_DRAW);
        
        setupAttributes(gl);
        gl.glBindVertexArray(0);
    }
    
    private void initForwardBuffers(GL3 gl) {
        // Forward line in -Z direction (cyan)
        float[] vertices = {
            // Single line pointing forward
            0.0f, 0.0f, 0.0f,   0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, -1.2f,  0.0f, 1.0f, 1.0f,  // Slightly longer than axes
        };
        
        gl.glGenVertexArrays(1, forwardVao, 0);
        gl.glBindVertexArray(forwardVao[0]);
        
        gl.glGenBuffers(1, forwardVbo, 0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, forwardVbo[0]);
        
        FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL3.GL_STATIC_DRAW);
        
        setupAttributes(gl);
        gl.glBindVertexArray(0);
    }
    
    private void setupAttributes(GL3 gl) {
        int stride = 6 * Float.BYTES; // 3 position + 3 color
        
        // Position attribute
        gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, stride, 0);
        gl.glEnableVertexAttribArray(0);
        
        // Color attribute
        gl.glVertexAttribPointer(1, 3, GL3.GL_FLOAT, false, stride, 3 * Float.BYTES);
        gl.glEnableVertexAttribArray(1);
    }
    
    public void render(GL3 gl, Mat4 transformWorld, Mat4 view, Mat4 projection, Vec3 cameraPos, boolean showForward) {
        // Disable depth test so axes are always visible
        gl.glDisable(GL3.GL_DEPTH_TEST);
        shader.use(gl);

        // Calculate distance from camera to transform origin
        Vec3 transformPos = new Vec3(
            transformWorld.get(0, 3),
            transformWorld.get(1, 3),
            transformWorld.get(2, 3)
        );
        float distance = Vec3.subtract(transformPos, cameraPos).magnitude();
        
        // scale based on distance to maintain constant screen space size
        // adjust multiplier to change screen size
        float distanceScale = distance * screenSpaceSize * 0.1f;
        
        // Scale axes
        Mat4 scale = Mat4.identity();
        scale.set(0, 0, distanceScale);
        scale.set(1, 1, distanceScale);
        scale.set(2, 2, distanceScale);
        
        Mat4 model = Mat4.multiply(transformWorld, scale);
        Mat4 mvp = Mat4.multiply(projection, Mat4.multiply(view, model));
        
        shader.setFloatArray(gl, "mvpMatrix", mvp.toFloatArrayForGLSL());
        
        // Draw axes (3 lines = 6 vertices)
        gl.glBindVertexArray(axesVao[0]);
        gl.glDrawArrays(GL3.GL_LINES, 0, 6);
        
        // Draw forward line (1 line = 2 vertices)
        if (showForward) {
            gl.glBindVertexArray(forwardVao[0]);
            gl.glDrawArrays(GL3.GL_LINES, 0, 2);
        }
        
        gl.glBindVertexArray(0);
         // Re-enable depth test for normal rendering
        gl.glEnable(GL3.GL_DEPTH_TEST);
    }
    
    public void dispose(GL3 gl) {
        gl.glDeleteBuffers(1, axesVbo, 0);
        gl.glDeleteVertexArrays(1, axesVao, 0);
        gl.glDeleteBuffers(1, forwardVbo, 0);
        gl.glDeleteVertexArrays(1, forwardVao, 0);
    }
}