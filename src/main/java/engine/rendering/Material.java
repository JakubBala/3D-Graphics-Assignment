package engine.rendering;

import engine.gmaths.*;
import java.util.HashMap;
import com.jogamp.opengl.GL3;
import engine.rendering.Shader;
import engine.gmaths.Vec3;

import java.util.List;
import java.util.Map;

/**
 * A flexible, shader-agnostic material system.
 * Materials simply store shader uniform values (floats, vectors, etc.)
 * that can be defined externally (e.g., from YAML files).
 */
public class Material {
    private final Shader shader;
    private final Map<String, Object> uniforms = new HashMap<>();

    public Material(GL3 gl, String vertexShaderPath, String fragmentShaderPath) {
        this.shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
    }

     public Shader getShader() {
        return shader;
    }

    public void setUniform(String name, Object value) {
        uniforms.put(name, value);
    }

    public Object getUniform(String name) {
        return uniforms.get(name);
    }

    /**
     * Applies all stored uniform values to the currently bound shader.
     */
    public void apply(GL3 gl) {
        for (Map.Entry<String, Object> entry : uniforms.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Number) {
                shader.setFloat(gl, name, ((Number) value).floatValue());
            } else if (value instanceof Vec3) {
                shader.setVec3(gl, name, (Vec3) value);
            } else if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (list.size() == 2)
                    shader.setFloat(gl, name, toFloat(list.get(0)), toFloat(list.get(1)));
                else if (list.size() == 3)
                    shader.setFloat(gl, name, toFloat(list.get(0)), toFloat(list.get(1)), toFloat(list.get(2)));
                else if (list.size() == 4)
                    shader.setFloat(gl, name, toFloat(list.get(0)), toFloat(list.get(1)), toFloat(list.get(2)), toFloat(list.get(3)));
            }
        }
    }

    public void setLightUniforms(GL3 gl,
        Vec3 lightPos,
        Vec3 lightAmbient,
        Vec3 lightDiffuse,
        Vec3 lightSpecular) 
    {
        shader.setVec3(gl, "light.position", lightPos);
        shader.setVec3(gl, "light.ambient", lightAmbient);
        shader.setVec3(gl, "light.diffuse", lightDiffuse);
        shader.setVec3(gl, "light.specular", lightSpecular);
    }

    public void setTransformUniforms(GL3 gl, 
        Mat4 modelMatrix, 
        Mat4 viewMatrix, 
        Mat4 projectionMatrix, 
        Vec3 viewPos) 
    {
        Mat4 mvpMatrix = Mat4.multiply(projectionMatrix, Mat4.multiply(viewMatrix, modelMatrix));
        
        shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());
        shader.setVec3(gl, "viewPos", viewPos);
    }

    private float toFloat(Object o) {
        if (o instanceof Number) return ((Number) o).floatValue();
        return Float.parseFloat(o.toString());
    }

    public void useShader(GL3 gl){
        shader.use(gl);
    }
}
