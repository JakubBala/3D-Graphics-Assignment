package engine.scripts;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;

import engine.components.Behaviour;
import engine.components.Camera;
import engine.components.Light;
import engine.components.Transform;
import engine.components.core.Renderable;
import engine.debug.BezierVisualizer;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.math.ArcLengthTable;
import engine.math.BezierCurve;
import engine.math.BezierPath;

public class BeeController extends Behaviour implements Renderable{

    // SERIALIZED
    public Transform leftWingPivot;
    public Transform rightWingPivot;
    public Transform buttPivot;
    public Transform leftEye;
    public Transform rightEye;

    // INTERNAL
    BezierVisualizer bezierVisualizer;
    BezierPath beePath;
    List<ArcLengthTable> arcLengthTables = new ArrayList<>();
    List<Float> pathLengths = new ArrayList<>();
    float totalPathLength = 0;

    float baseCycleSpeed = 20f;

    int detected = 0;
    float fear = 0;
    float fearRampUp = 0.04f;
    float fearRampDown = 0.04f;

    Vec3 baseEyeScale = new Vec3(0.45f ,0.7f ,0.6f);
    float fearMultipler = 0.5f;

    @Override
    public void Start(){
        // Bezier visualisation shit
        GL3 glContext = getGameObject().getScene().getGLcontext();
        bezierVisualizer = new BezierVisualizer(glContext);
        fear = 0;
        BuildBezierPath();
        InitializeBezierVisualizer();
    }

    @Override
    public void Update(){
        BeeFollowBezier();
        BeeOscillateVertically();
        BeeAnimationPassive();

        if(detected > 0){
            RampFearUp();
        }
        else{
            RampFearDown();
        }
        FearEffects();
    }

    private void BeeFollowBezier(){
        float time = (float) GameController.getElapsedTime();
        float circuitTime = baseCycleSpeed; // seconds per full loop
        float normalized = (time % circuitTime) / circuitTime;  // 0 -> 1

        // Convert normalized time to distance along path
        float distance = normalized * totalPathLength;

        // Find correct curve index and distance along that curve
        int curveIndex = 0;
        float remaining = distance;

        while (curveIndex < pathLengths.size() &&
               remaining > pathLengths.get(curveIndex)) {
            remaining -= pathLengths.get(curveIndex);
            curveIndex++;
        }

        // Clamp for safety
        curveIndex = Math.min(curveIndex, pathLengths.size() - 1);

        // Use arc-length table to convert distance to u parameter
        ArcLengthTable table = arcLengthTables.get(curveIndex);
        float localU = table.distanceToU(remaining);

        // Evaluate actual curve
        BezierCurve curve = beePath.getCurve(curveIndex);
        Vec3 bezierPos = curve.evaluate(localU);

        // Set bee position
        getGameObject().getTransform().SetLocalPosition(
            bezierPos.x, bezierPos.y, bezierPos.z
        );

        // Orient bee to face forward along the path
        Vec3 tangent = curve.tangentNormalized(localU);
        Vec3 lookTarget = Vec3.add(bezierPos, tangent);  // Point ahead along tangent
        lookTarget.y = bezierPos.y;
        
        getGameObject().getTransform().LookAt(
            lookTarget,        
            new Vec3(0, 1, 0)// world up vector
        );
    }

    // adds extra y oscilation
    private void BeeOscillateVertically(){
        float time = (float) GameController.getElapsedTime();
        float bobSpeed   = 4f;
        float bobAmount  = 0.3f;
        float delta = (float)Math.sin(time * bobSpeed) * bobAmount;
        // apply vertical translation
        getGameObject().getTransform().Translate(new Vec3(0, delta, 0));
    }

    // Bee constantly animates this unconditionally.
    private void BeeAnimationPassive(){
        float time = (float) GameController.getElapsedTime();

        // --- WINGS ---
        float flapSpeed = 60f;
        float amplitude = 30f; // degrees
        float wave = (float)Math.sin(time * flapSpeed);
        float leftY  = wave * amplitude;
        float rightY = wave * amplitude;   // mirror
        // LEFT WING
        Vec3 leftRot = leftWingPivot.GetRotation();
        leftWingPivot.SetLocalRotation(leftRot.x, leftY, leftRot.z);

        // RIGHT WING
        Vec3 rightRot = rightWingPivot.GetRotation();
        rightWingPivot.SetLocalRotation(rightRot.x, rightY, rightRot.z);

        // --- BUTT WAGGLE ---
        float buttSpeed = 4f;
        float buttMin = 15f;
        float buttMax = 25f;
        float buttBase = (buttMin + buttMax) * 0.5f;
        float buttAmp = (buttMax - buttMin) * 0.5f;

        float buttWave = (float)Math.sin(time * buttSpeed);
        float buttX = buttBase + buttWave * buttAmp;

        Vec3 bRot = buttPivot.GetRotation();
        buttPivot.SetLocalRotation(buttX, bRot.y, bRot.z);
    }

    public void ChangeDetectedCounter(int i){
        detected = Math.max(0, Math.min(3, detected + i));
    }

    private void RampFearUp(){
        // increase fear per frame
        fear = Math.min(1f, fear + fearRampUp);
    }

    private void RampFearDown(){
        // decrease fear per frame
        fear = Math.max(0f, fear - fearRampDown);
    }

    private void FearEffects(){
        if(fear <= 0) return;

        // increase eye size
        float scaleMultiplier = 1 + (fear * fearMultipler);
        Vec3 eyeScale = Vec3.multiply(baseEyeScale, scaleMultiplier);
        leftEye.SetLocalScale(eyeScale.x, eyeScale.y, eyeScale.z);
        rightEye.SetLocalScale(eyeScale.x, eyeScale.y, eyeScale.z);
    }

    private void BuildBezierPath(){
        beePath = new BezierPath();
        BezierCurve curve1 = new BezierCurve(
            new Vec3(0, 2, -5),
            new Vec3(3f, 2, -5f),
            new Vec3(5f, 2, -3f),
            new Vec3(5, 2, 0)
        );
        beePath.addCurve(curve1);

        BezierCurve curve2 = BezierCurve.createC1Continuation(
            curve1,
            new Vec3(3f, 2f, 3f),
            new Vec3(0, 2f, 5f)
        );
        beePath.addCurve(curve2);

        BezierCurve curve3 = BezierCurve.createC1Continuation(
            curve2,
            new Vec3(-7f, 2f, 0f),
            new Vec3(-3f, 2f, 0)
        );
        beePath.addCurve(curve3);

        BezierCurve curve4 = BezierCurve.createC1Continuation(
            curve3,
            new Vec3(0f, 2f, 4f),
            new Vec3(-4f, 2f, 4f)
        );
        beePath.addCurve(curve4);

        BezierCurve curve5 = BezierCurve.createC1Continuation(
            curve4,
            new Vec3(-6f, 2f, 2f),
            new Vec3(-5f, 2f, 0f)
        );
        beePath.addCurve(curve5);

        BezierCurve curve6 = BezierCurve.createC1Continuation(
            curve5,
            new Vec3(-4f, 2f, -4f),
            new Vec3(-5f, 2f, -5f)
        );
        beePath.addCurve(curve6);

        BezierCurve curve7 = BezierCurve.createC1Continuation(
            curve6,
            new Vec3(-5f, 2f, -7f),
            new Vec3(-3f, 2f, -6f)
        );
        beePath.addCurve(curve7);

        BezierCurve curve8 = BezierCurve.createC1Continuation(
            curve7,
            new Vec3(-1f, 2f, -5f),
            new Vec3(0f, 2f, -5f)    // END = curve1.P0 (closing the loop)
        );
        beePath.addCurve(curve8);
        beePath.setClosed(true);

        // Build arclength tables for each curve
        int tableSamples = 50;
        for (BezierCurve c : beePath.getCurves()) {
            ArcLengthTable table = c.buildArcLengthTable(tableSamples);
            arcLengthTables.add(table);
            
            float L = table.getTotalLength();
            pathLengths.add(L);
            totalPathLength += L;
        }
    }

    private void InitializeBezierVisualizer(){
        bezierVisualizer.setLineColor(1.0f, 0.5f, 0.0f); // Oring line
        bezierVisualizer.setSamplesPerCurve(50); // Smoothness
    }

    @Override
    public void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition, List<Light> lights) {
        bezierVisualizer.updatePath(gl, beePath);
        bezierVisualizer.render(gl, view, projection);
    }
}
