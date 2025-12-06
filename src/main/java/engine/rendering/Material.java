package engine.rendering;

import engine.components.Light;
import engine.gmaths.*;
import java.util.HashMap;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;

import engine.rendering.Shader;

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
    private final Map<String, Texture> textures = new HashMap<>();
    private boolean doubleSided = false;

    public Material(GL3 gl, String vertexShaderPath, String fragmentShaderPath) {
        this.shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
    }

    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }   

    public Shader getShader() {
        return shader;
    }

    public boolean hasUniform(String name) {
        return uniforms.containsKey(name);
    }

    public void setUniform(String name, Object value) {
        uniforms.put(name, value);
    }

    public Object getUniform(String name) {
        return uniforms.get(name);
    }

    public void setTexture(String name, Texture texture) {
        if (texture != null) textures.put(name, texture);
    }

    public Texture getTexture(String name) {
        return textures.get(name);
    }

    /**
     * Applies all stored uniform values to the currently bound shader.
     */
    public void apply(GL3 gl) {

        // Disable culling if doublesided
        if (doubleSided) {
            gl.glDisable(GL3.GL_CULL_FACE);
        }

        // set scalar/vector uniforms
        for (Map.Entry<String, Object> entry : uniforms.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if(value instanceof Integer){
                shader.setInt(gl, name, ((Integer) value).intValue());
            } else if (value instanceof Number) {
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

        // Read tiling uniform (if present) so we can decide texture wrap.
        float tileU = 1.0f, tileV = 1.0f;
        Object tilingObj = uniforms.get("material.tiling");
        if (tilingObj instanceof List<?>) {
            List<?> l = (List<?>) tilingObj;
            if (l.size() >= 2) {
                tileU = toFloat(l.get(0));
                tileV = toFloat(l.get(1));
            }
        }

        // bind textures to units
        int unit = 0;
        for (var entry : textures.entrySet()) {
            Texture tex = entry.getValue();
            if (tex == null) continue;
            gl.glActiveTexture(GL3.GL_TEXTURE0 + unit);
            tex.bind(gl);
            shader.setInt(gl, entry.getKey(), unit); // e.g. "material.diffuseMap" = 0

            // set wrap mode according to tiling
            if (tileU != 1.0f || tileV != 1.0f) {
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT);
            } else {
                shader.setFloat(gl, "material.tiling", 1.0f, 1.0f);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
            }

            unit++;
        }
    }

    public void setLightsUniform(GL3 gl, List<Light> lights) {
        int numLights = Math.min(lights.size(), 8); // Max 8 lights
        shader.setInt(gl, "numActiveLights", numLights);
        for (int i = 0; i < numLights; i++) {
            Light light = lights.get(i);
            String prefix = "lights[" + i + "].";
            
            shader.setVec3(gl, prefix + "position", light.getPosition());
            // Send raw color components; shader will scale by light.intensity
            shader.setVec3(gl, prefix + "ambient", light.getAmbient());
            shader.setVec3(gl, prefix + "diffuse", light.getDiffuse());
            shader.setVec3(gl, prefix + "specular", light.getSpecular());
            shader.setFloat(gl, prefix + "intensity", light.getIntensity());
            
            shader.setInt(gl, prefix + "type", light.getType().ordinal());
            
            // Attenuation
            shader.setFloat(gl, prefix + "constant", light.getConstant());
            shader.setFloat(gl, prefix + "linear", light.getLinear());
            shader.setFloat(gl, prefix + "quadratic", light.getQuadratic());
            
            // Spotlight
            // Always set a direction so the shader can use it for spot/directional lights
            shader.setVec3(gl, prefix + "direction", light.getDirection());

            if (light.getType() == Light.LightType.SPOT) {
                shader.setVec3(gl, prefix + "direction", light.getDirection());
                shader.setFloat(gl, prefix + "cutOff", 
                    (float)Math.cos(Math.toRadians(light.getCutOff())));
                shader.setFloat(gl, prefix + "outerCutOff", 
                    (float)Math.cos(Math.toRadians(light.getOuterCutOff())));
            }
        }
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

    // Call this after rendering to restore culling
    public void restore(GL3 gl) {
        if (doubleSided) {
            gl.glEnable(GL3.GL_CULL_FACE);
        }
    }
}
