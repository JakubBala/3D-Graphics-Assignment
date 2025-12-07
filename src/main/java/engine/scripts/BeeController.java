package engine.scripts;

import java.util.List;

import com.jogamp.opengl.GL3;

import engine.components.Behaviour;
import engine.components.Camera;
import engine.components.Light;
import engine.components.core.Renderable;
import engine.debug.BezierVisualizer;
import engine.gmaths.Mat4;
import engine.gmaths.Vec3;
import engine.math.BezierCurve;
import engine.math.BezierPath;

public class BeeController extends Behaviour implements Renderable{

    BezierVisualizer bezierVisualizer;
    BezierPath beePath;

    @Override
    public void Start(){
        // Bezier visualisation shit
        GL3 glContext = getGameObject().getScene().getGLcontext();
        bezierVisualizer = new BezierVisualizer(glContext);
        BuildBezierPath();
        InitializeBezierVisualizer();
    }

    @Override
    public void Update(){
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
            new Vec3(3f, 2f, 5f),
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
