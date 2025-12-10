package engine.scripts;

import engine.components.Behaviour;
import engine.components.Light;
import engine.components.Transform;
import engine.gmaths.Vec3;

public class GlobalLightController extends Behaviour {
    // attached to global light
    
    // 0.0 = midnight, 0.25 = dawn, 0.5 = midday, 0.75 = dusk, 1.0 = midnight

    // Time (0-1) when the light intensity finishes being 0 and starts fading up.
    private final float FADE_IN_START = 0.05f; 
    
    // Time (0-1) when the light intensity reaches 1.0 and starts fading down.
    private final float FADE_IN_END = 0.45f;
    private final float FADE_OUT_START = 0.55f;   
    
    // Time (0-1) when the light intensity finishes fading down and returns to 0.
    private final float FADE_OUT_END = 0.95f; 
    

    float baseIntensity = 1f;

    float globalLightIntensityMult = 1f;

    Light sunLight;
    Transform sunTransform;

    @Override 
    public void Start(){
        sunLight = getGameObject().getComponent(Light.class);
        baseIntensity = sunLight.getIntensity();
        sunTransform = getGameObject().getComponent(Transform.class);
        globalLightIntensityMult = 1f;
    }

    @Override
    public void Update(){
        float cycleTime = GameController.getDayLightCycle();
        float intensityMultiplier = calculateIntensityMultiplier(cycleTime);
        sunLight.setIntensity(baseIntensity * intensityMultiplier * globalLightIntensityMult);

        float rotationAngleX = calculateRotationAngle(cycleTime);

        Vec3 rotation = new Vec3(sunTransform.GetRotation());
        sunTransform.SetLocalRotation(rotationAngleX, rotation.y, rotation.z);
    }

    public void setGlobalLightIntensityMultiplier(float v){
        globalLightIntensityMult = v;
    }

    private float calculateIntensityMultiplier(float cycleTime) {
        float multiplier;

        if (cycleTime < FADE_IN_START) {
            // Region 1: (Night)
            multiplier = 0.0f;
            
        } else if (cycleTime < FADE_IN_END) {
            // Region 2: FADE_IN_START to FADE_IN_END (Fade In / Sunrise)
            float fadeDuration = FADE_IN_END - FADE_IN_START;
            
            // Calculate linear progress from 0.0 to 1.0
            multiplier = (cycleTime - FADE_IN_START) / fadeDuration;
            
        } else if (cycleTime < FADE_OUT_START) {
            // Region 3: FADE_IN_END to FADE_OUT_START (Peak Day)
            // The light is fully on.
            multiplier = 1.0f;
            
        } else if (cycleTime < FADE_OUT_END) {
            // Region 4: FADE_OUT_START to FADE_OUT_END (Fade Out / Sunset)
            float fadeDuration = FADE_OUT_END - FADE_OUT_START;
            
            // Calculate linear progress (0.0 to 1.0) and invert it (1.0 -> 0.0)
            float progress = (cycleTime - FADE_OUT_START) / fadeDuration;
            
            multiplier = 1.0f - progress;
            
        } else {
            // Region 5: (Night)
            multiplier = 0.0f;
        }

        return Math.max(0.0f, Math.min(multiplier, 1.0f));
    }

    private float calculateRotationAngle(float cycleTime) {
        float angle;

        if (cycleTime < FADE_IN_START) {
            // Region 1: (Night/Pre-Dawn)
            // Stays at the start position 
            angle = 0.0f;
            
        } else if (cycleTime < FADE_IN_END) {
            // Region 2:(Sunrise: 0 -> -90 degrees)
            float segmentDuration = FADE_IN_END - FADE_IN_START; 
            
            // Calculate progress (0.0 to 1.0)
            float progress = (cycleTime - FADE_IN_START) / segmentDuration;
            
            // Interpolate from 0 degrees to -90 degrees
            angle = (0.0f * (1.0f - progress)) + (-90.0f * progress); 
            
        } else if (cycleTime < FADE_OUT_START) {
            // Region 3: 0.4 to 0.5 (Midday Plateau: -90 degrees)
            angle = -90.0f;
            
        } else if (cycleTime < FADE_OUT_END) {
            // Region 4: (Sunset: -90 -> -180 degrees)
            float segmentDuration = FADE_OUT_END - FADE_OUT_START;
            
            // Calculate progress (0.0 to 1.0)
            float progress = (cycleTime - FADE_OUT_START) / segmentDuration;
            
            // Interpolate from -90 degrees to -180 degrees
            angle = (-90.0f * (1.0f - progress)) + (-180.0f * progress); 
            
        } else {
            // Region 5: (Night/Post-Dusk)
            // Stays at the end position (or previous position)
            angle = -180.0f;
        }

        return angle;
    }
}
