package engine.data;

import com.jogamp.opengl.GL3;

import engine.components.Light;
import engine.components.core.Component;
import engine.gmaths.Vec3;
import engine.scene.GameObject;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class LightSpec extends ComponentSpec {
    // Light configuration from YAML
    public String lightType;        // "directional", "point", "spot"
    public float[] ambient;         // [r, g, b]
    public float[] diffuse;         // [r, g, b]
    public float[] specular;        // [r, g, b]
    public Float intensity;         // Optional
    public Float constant;          // Attenuation
    public Float linear;
    public Float quadratic;
    public Float cutOff;            // Spotlight
    public Float outerCutOff;
    public Boolean enabled;
    
    @Override
    public Component createComponent(GameObject gameObject, GL3 gl) {
        Light light = new Light();
        
        // Set light type
        if (lightType != null) {
            switch (lightType.toLowerCase()) {
                case "directional":
                    light.setType(Light.LightType.DIRECTIONAL);
                    break;
                case "point":
                    light.setType(Light.LightType.POINT);
                    break;
                case "spot":
                    light.setType(Light.LightType.SPOT);
                    break;
            }
        }
        
        // Set colors
        if (ambient != null && ambient.length == 3) {
            light.setAmbient(new Vec3(ambient[0], ambient[1], ambient[2]));
        }
        if (diffuse != null && diffuse.length == 3) {
            light.setDiffuse(new Vec3(diffuse[0], diffuse[1], diffuse[2]));
        }
        if (specular != null && specular.length == 3) {
            light.setSpecular(new Vec3(specular[0], specular[1], specular[2]));
        }
        
        // Optional parameters
        if (intensity != null) light.setIntensity(intensity);
        if (constant != null) light.setConstant(constant);
        if (linear != null) light.setLinear(linear);
        if (quadratic != null) light.setQuadratic(quadratic);
        if (cutOff != null) light.setCutOff(cutOff);
        if (outerCutOff != null) light.setOuterCutOff(outerCutOff);
        if (enabled != null) light.setEnabled(enabled);
        
        System.out.println("[LightSpec]: Created " + lightType + " light");
        return light;
    }
}
