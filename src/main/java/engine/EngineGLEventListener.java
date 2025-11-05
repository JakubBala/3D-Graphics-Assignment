package engine;

import com.jogamp.opengl.*;
import engine.gmaths.*;
import engine.components.Camera;

public class EngineGLEventListener implements GLEventListener {
    private final Camera camera;
    private double startTime;

    public EngineGLEventListener(Camera camera) {
        this.camera = camera;
        this.camera.setPosition(new Vec3(-10, 6, 20));
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClearColor(0, 0, 0, 1);
        gl.glEnable(GL.GL_DEPTH_TEST);
        startTime = getSeconds();

        // TODO: Later → load your YAML scene here
        System.out.println("EngineGLEventListener initialized.");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(x, y, width, height);
        float aspect = (float) width / height;
        camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // TODO: Replace with scene.render(gl)
        renderDebugCube(gl);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("Disposing GL resources...");
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

    private void renderDebugCube(GL3 gl) {
        // placeholder until you hook up Scene rendering
    }
}