package engine.components;
import engine.components.core.Component;
import engine.gmaths.Vec3;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class Light extends Component{
    
    //Light types
    public enum LightType {
        DIRECTIONAL,  // Sun-like, no attenuation
        POINT,        // Omnidirectional, attenuates with distance
        SPOT          // Cone-shaped, like flashlight
    }

    private LightType type = LightType.POINT;

    // Colors
    private Vec3 ambient = new Vec3(0.2f, 0.2f, 0.2f);
    private Vec3 diffuse = new Vec3(1.0f, 1.0f, 1.0f);
    private Vec3 specular = new Vec3(1.0f, 1.0f, 1.0f);

    // Attenuation (for point/spot lights)
    private float constant = 1.0f;
    private float linear = 0.09f;
    private float quadratic = 0.032f;

    // Spotlight specific
    private float cutOff = 12.5f;        // Inner cone angle (degrees)
    private float outerCutOff = 17.5f;   // Outer cone angle (degrees)

    // Intensity multiplier
    private float intensity = 1.0f;
    
    // Enable/disable
    private boolean enabled = true;

    // Getters/Setters
    public LightType getType() { return type; }
    public void setType(LightType type) { this.type = type; }
    
    public Vec3 getAmbient() { return ambient; }
    public void setAmbient(Vec3 ambient) { this.ambient = ambient; }
    
    public Vec3 getDiffuse() { return diffuse; }
    public void setDiffuse(Vec3 diffuse) { this.diffuse = diffuse; }
    
    public Vec3 getSpecular() { return specular; }
    public void setSpecular(Vec3 specular) { this.specular = specular; }
    
    public float getConstant() { return constant; }
    public void setConstant(float constant) { this.constant = constant; }
    
    public float getLinear() { return linear; }
    public void setLinear(float linear) { this.linear = linear; }
    
    public float getQuadratic() { return quadratic; }
    public void setQuadratic(float quadratic) { this.quadratic = quadratic; }
    
    public float getCutOff() { return cutOff; }
    public void setCutOff(float cutOff) { this.cutOff = cutOff; }
    
    public float getOuterCutOff() { return outerCutOff; }
    public void setOuterCutOff(float outerCutOff) { this.outerCutOff = outerCutOff; }
    
    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Get world position from Transform
    public Vec3 getPosition() {
        return gameObject.getTransform().GetWorldPosition();
    }
    
    // Get forward direction for directional/spot lights
    public Vec3 getDirection() {
        // Get forward vector from transform rotation
        // Assuming forward is -Z in local space
        return gameObject.getTransform().GetForward();
    }
}
