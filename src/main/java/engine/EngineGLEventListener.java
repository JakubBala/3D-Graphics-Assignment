package engine;

import com.jogamp.opengl.*;

import assets.models.Cube;
import assets.models.Pyramid;
import assets.models.Sphere;
import engine.gmaths.*;
import engine.rendering.Mesh;
import engine.rendering.Shader;
import engine.components.Camera;
import engine.debug.DebugGrid;
import engine.loaders.MaterialLoader;
import engine.loaders.SceneLoader;
import engine.rendering.Material;
import engine.scene.*;

public class EngineGLEventListener implements GLEventListener {

    private double startTime;

    Scene activeScene;
    DebugGrid debugGrid;

    public EngineGLEventListener() {
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); 
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
        gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
        gl.glCullFace(GL.GL_BACK);    // default is 'back', assuming CCW

        initialise(gl);

        System.out.println("EngineGLEventListener initialized.");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(x, y, width, height);

        activeScene.windowResized(width, height);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        render(gl);
    }

    @Override
    /* Clean up memory, if necessary */
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("Disposing GL resources...");
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

    public void initialise(GL3 gl){
        startTime = getSeconds();
        // TODO: Load your YAML scene here
        // TODO: Call OnStart
        activeScene = SceneLoader.Load("assets/scenes/testing.yaml", gl);
        activeScene.findAndSetMainCamera();
        debugGrid = new DebugGrid(gl);
    }

    public void render(GL3 gl){
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        
        float dayLightCycle = (float)((getSeconds() - startTime) / 20.0 % 1.0); // 60 seconds for full cycle
        System.out.println("Daylight cycle time: " + dayLightCycle);
        activeScene.GetSkybox().SetTimeOfDay(dayLightCycle);

        activeScene.render(gl);

        Camera mainCamera = activeScene.getMainCameraInstance();
        debugGrid.render(gl, mainCamera.getViewMatrix(), mainCamera.getPerspectiveMatrix());
    }

    public void keyboardInput(Camera.Movement movement) {
        activeScene.keyboardInput(movement);
    }

    public void mouseInput(float dx, float dy) {
        activeScene.mouseInput(dx, dy);
    }
}