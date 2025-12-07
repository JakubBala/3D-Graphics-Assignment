package engine.debug;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;

import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.math.BezierCurve;
import engine.math.BezierPath;
import engine.rendering.Shader;

public class BezierVisualizer {
    private Shader shader;
    private int[] vao = new int[1];
    private int[] vbo = new int[1];
    private int vertexCount = 0;
    
    private int samplesPerCurve = 50;
    private Vec3 lineColor = new Vec3(1.0f, 1.0f, 1.0f); // White by default
    
    public BezierVisualizer(GL3 gl) {
        this.shader = new Shader(gl, "assets/shaders/vs_debug_axes.vert", "assets/shaders/fs_debug_axes.frag");
        gl.glGenVertexArrays(1, vao, 0);
        gl.glGenBuffers(1, vbo, 0);
    }
    
    public void setSamplesPerCurve(int samples) {
        this.samplesPerCurve = samples;
    }
    
    public void setLineColor(Vec3 color) {
        this.lineColor = new Vec3(color);
    }
    
    public void setLineColor(float r, float g, float b) {
        this.lineColor = new Vec3(r, g, b);
    }
    
    /**
     * Update buffer for a single curve
     */
    public void updateCurve(GL3 gl, BezierCurve curve) {
        List<BezierCurve> curves = new ArrayList<>();
        curves.add(curve);
        updateCurves(gl, curves);
    }
    
    /**
     * Update buffer for a path
     */
    public void updatePath(GL3 gl, BezierPath path) {
        updateCurves(gl, path.getCurves());
    }
    
    /**
     * Update buffer for multiple curves
     */
    public void updateCurves(GL3 gl, List<BezierCurve> curves) {
        List<Float> vertices = new ArrayList<>();
        
        for (BezierCurve curve : curves) {
            // Sample curve points
            List<Vec3> points = curve.sample(samplesPerCurve);
            
            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 p1 = points.get(i);
                Vec3 p2 = points.get(i + 1);
                
                // First point
                vertices.add(p1.x); 
                vertices.add(p1.y); 
                vertices.add(p1.z);
                vertices.add(lineColor.x); 
                vertices.add(lineColor.y); 
                vertices.add(lineColor.z);
                
                // Second point
                vertices.add(p2.x); 
                vertices.add(p2.y); 
                vertices.add(p2.z);
                vertices.add(lineColor.x); 
                vertices.add(lineColor.y); 
                vertices.add(lineColor.z);
            }
        }
        
        // Upload to GPU
        uploadBuffer(gl, vertices);
        vertexCount = vertices.size() / 6;
    }
    
    private void uploadBuffer(GL3 gl, List<Float> vertices) {
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }
        
        gl.glBindVertexArray(vao[0]);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo[0]);
        
        FloatBuffer fb = Buffers.newDirectFloatBuffer(vertexArray);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, Float.BYTES * vertexArray.length, fb, GL3.GL_DYNAMIC_DRAW);
        
        int stride = 6 * Float.BYTES; // 3 position + 3 color
        
        // Position attribute
        gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, stride, 0);
        gl.glEnableVertexAttribArray(0);
        
        // Color attribute
        gl.glVertexAttribPointer(1, 3, GL3.GL_FLOAT, false, stride, 3 * Float.BYTES);
        gl.glEnableVertexAttribArray(1);
        
        gl.glBindVertexArray(0);
    }
    
    /**
     * Render the bezier curve path
     */
    public void render(GL3 gl, Mat4 view, Mat4 projection) {
        if (vertexCount == 0) return;
        
        //gl.glDisable(GL3.GL_DEPTH_TEST);
        gl.glLineWidth(2.0f);
        
        shader.use(gl);
        
        Mat4 mvp = Mat4.multiply(projection, view);
        shader.setFloatArray(gl, "mvpMatrix", mvp.toFloatArrayForGLSL());
        
        gl.glBindVertexArray(vao[0]);
        gl.glDrawArrays(GL3.GL_LINES, 0, vertexCount);
        gl.glBindVertexArray(0);
        
        gl.glLineWidth(1.0f);
        gl.glEnable(GL3.GL_DEPTH_TEST);
    }
    
    public void dispose(GL3 gl) {
        gl.glDeleteBuffers(1, vbo, 0);
        gl.glDeleteVertexArrays(1, vao, 0);
    }
}
