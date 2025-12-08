package engine.scripts;

import engine.components.Behaviour;
import engine.components.MeshRenderer;
import engine.components.Transform;
import engine.gmaths.Vec3;
import engine.rendering.Material;
import engine.rendering.Shader;

public class MenhirController extends Behaviour{

    // SERIALIZED
    public BeeController beeController;
    public Transform pivot;

    // INTERNAL
    private float anger;
    private float angerRampSpeed = 0.01f;
    private float baseEmission = 0.5f;
    private float maxEmission = 1.0f;
    private float baseMaldingSpeed = 10.0f;
    private float maxMaldingSpeed = 30.0f;
    private float maxMaldingAmplitude = 1.0f;
    private float shakePhase = 0f;
    private MeshRenderer mr;

    @Override
    public void Start(){
        anger = 0f;
        mr = getGameObject().getComponent(MeshRenderer.class);
    }

    @Override
    public void Update(){
        ScanForBee();

        if(anger > 0){
            AnimateMenhirAnger();
        }
    }    


    private void ScanForBee(){
        Vec3 beePos = new Vec3(beeController.getGameObject().getTransform().GetWorldPosition());
        Vec3 forward = getGameObject().getTransform().GetForward();

        Vec3 myPos = getGameObject().getTransform().GetWorldPosition();
        float distance = myPos.distance(beePos);

        float dot = Vec3.dotProduct(forward, beePos);
        // bee is 45f degrees either side of forward
        if(distance < 5f && dot > 0.5f){

            // ramp anger if in front
            RampAngerUp();
            return;
        }

        RampAngerDown();
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
        float dt = (float) GameController.getDeltaTime(); // recommended if available
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
        //mr.getMaterial().setUniform("emissionStrength", emission);
    }
}
