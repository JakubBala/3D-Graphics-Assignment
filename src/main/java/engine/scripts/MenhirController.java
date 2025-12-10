package engine.scripts;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;

import engine.components.Behaviour;
import engine.components.MeshRenderer;
import engine.components.Transform;
import engine.gmaths.Vec3;
import engine.rendering.Material;
import engine.rendering.Shader;
import engine.rendering.TextureLibrary;

public class MenhirController extends Behaviour{

    // SERIALIZED REFERENCES
    public BeeController beeController;
    public Transform pivot;

    // SERIALIZED PROPERTIES
    public List<String> frameStrings;

    // INTERNAL
    private int state = 0;   // 0 = getting less angry or not angry, 1 = getting angry or fully angry
    private float stateChangeTime = 0f;
    private float anger;
    private float angerRampSpeed = 0.01f;
    private float baseEmission = 0.5f;
    private float maxEmission = 1.0f;
    private float baseMaldingSpeed = 10.0f;
    private float maxMaldingSpeed = 30.0f;
    private float maxMaldingAmplitude = 1.0f;
    private float shakePhase = 0f;
    private MeshRenderer mr;
    private ArrayList<Texture> animationFrames;
    private int startFrameAtStateChange = 0;
    private int currentFrameIndex = 0;
    private float frameInterval = 0.5f;

    @Override
    public void Awake(){
        LoadAnimationFrames();
    }

    @Override
    public void Start(){
        anger = 0f;
        mr = getGameObject().getComponent(MeshRenderer.class);
    }

    @Override
    public void Update(){
        ScanForBee();

        if(state == 0){
            RampAngerDown();
            AnimateFramesDownward();
        }
        else if(state == 1){
            RampAngerUp();
            AnimateFramesUpward();
        }
        if(anger > 0){
            AnimateMenhirAnger();
        }
    }   
    
    private void LoadAnimationFrames(){
        animationFrames = new ArrayList<>();
        if(frameStrings == null) return;
        for (String frameString : frameStrings) {
            GL3 gl = getGameObject().getScene().getGLcontext();
            animationFrames.add(TextureLibrary.LoadTexture(gl, frameString));
        }
        currentFrameIndex = 0;
    }


    private void ScanForBee(){

        Vec3 beePos = new Vec3(beeController.getGameObject().getTransform().GetWorldPosition());
        Vec3 forward = getGameObject().getTransform().GetForward();

        Vec3 myPos = getGameObject().getTransform().GetWorldPosition();
        Vec3 diff = Vec3.subtract(beePos, myPos);
        float distance = Vec3.magnitude(diff);
        Vec3 direction = Vec3.normalize(diff);

        float dot = Vec3.dotProduct(forward, direction);
        // bee is 45f degrees either side of forward
        if(distance < 5f && dot > 0.5f){

            ChangeState(1);
            return;
        }

        ChangeState(0);
    }

    private void ChangeState(int newState){
        if(state == newState) return;
        stateChangeTime = (float)GameController.getElapsedTime();
        startFrameAtStateChange = currentFrameIndex;
        state = newState;
    }

    private void RampAngerUp(){
        // increase anger per frame
        anger = Math.min(1f, anger + angerRampSpeed);

    }

    private void RampAngerDown(){
        // decrease anger per frame
        anger = Math.max(0f, anger - angerRampSpeed);
    }


    // make menhir shake based on anger and change emission power
    private void AnimateMenhirAnger(){
        // Use per-frame delta time instead of absolute time * speed
        float dt = (float) GameController.getDeltaTime();
        if (dt <= 0f) {
            // fallback to small dt to avoid stalls
            dt = 1f / 60f;
        }
        // compute current speed and amplitude (interpolated by anger)
        float maldingSpeed = baseMaldingSpeed + anger * (maxMaldingSpeed - baseMaldingSpeed);
        float maldingAmp   = anger * maxMaldingAmplitude;

        // advance phase smoothly by dt * speed (no discontinuities when speed changes)
        shakePhase += dt * maldingSpeed;

        // keep phase from growing unbounded (might be overkill)
        if (shakePhase > Float.POSITIVE_INFINITY / 2) {
            shakePhase = shakePhase % ((float)Math.PI * 2f);
        }

        float wave = (float) Math.sin(shakePhase);
        float shakeZ = wave * maldingAmp;
        Vec3 currentRot = pivot.GetRotation();
        pivot.SetLocalRotation(
            currentRot.x,
            currentRot.y,
            shakeZ
        );

        // change emission colour based on anger
        float emission = baseEmission + (anger * (maxEmission - baseEmission));
        mr.getMaterial().setUniform("emissionStrength", emission);
    }


    // Change the current emission texture from index 0 -> 4 using the loaded texture frames
    private void AnimateFramesUpward(){
        if (animationFrames == null || animationFrames.isEmpty()) return;
        final int frameCount = animationFrames.size();
        // If we’re already at the top frame, do nothing
        if (currentFrameIndex >= frameCount - 1) return;

        float timeNow = (float)GameController.getElapsedTime();
        float elapsed = timeNow - stateChangeTime;

        // how many intervals have passed
        int framesPassed = (int)(elapsed / frameInterval);
        int targetFrame = startFrameAtStateChange + framesPassed;
        // Clamp so we never exceed last frame
        targetFrame = Math.min(targetFrame, frameCount - 1);
        // Only update material if index changed
        if (targetFrame != currentFrameIndex) {
            currentFrameIndex = targetFrame;
            mr.getMaterial().setTexture("material.emissionMap", animationFrames.get(targetFrame));
        }
    }

    // Same as upward, but other way
    private void AnimateFramesDownward(){
        if (animationFrames == null || animationFrames.isEmpty()) return;
        // If we're already at the lowest frame, stop.
        if (currentFrameIndex <= 0) return;

        float now = (float) GameController.getElapsedTime();
        float elapsed = now - stateChangeTime;

        // How many frames we should have stepped down
        int framesDown = (int)(elapsed / frameInterval);
        // Compute target frame: current minus how many should have passed
        int targetFrame = startFrameAtStateChange - framesDown;
        // Clamp so we never go below zero
        targetFrame = Math.max(0, targetFrame);
        // Only update when the frame actually changes
        if (targetFrame != currentFrameIndex) {
            currentFrameIndex = targetFrame;
            mr.getMaterial().setTexture("material.emissionMap", animationFrames.get(targetFrame));
        }
    }
}
