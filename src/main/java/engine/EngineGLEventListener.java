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

    private Shader shaderSphere, shaderLight;
    private final Camera camera;
    private double startTime;


    // TODO: This will go into GameObjects themselves
    private Mesh light;

    private Vec3 lightPosition = new Vec3(4f,5f,8f);
    private Vec3 lightAmbient = new Vec3(0.2f, 0.2f, 0.2f);
    private Vec3 lightDiffuse = new Vec3(0.0f, 0.9f, 0.9f);
    private Vec3 lightSpecular = new Vec3(0.9f, 0.9f, 0.9f);

    Scene activeScene;
    DebugGrid debugGrid;

    public EngineGLEventListener(Camera camera) {
        this.camera = camera;
        this.camera.setPosition(new Vec3(-10, 6, 20));
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

        // TODO: This can be for the most part moved to the Camera.
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(x, y, width, height);
        float aspect = (float) width / height;
        camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
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

        light = new Mesh(gl, Sphere.vertices, Sphere.indices);
        shaderLight = new Shader(gl, "assets/shaders/vs_light_01.vert", "assets/shaders/fs_light_01.frag");

        debugGrid = new DebugGrid(gl);
        activeScene = SceneLoader.Load("assets/scenes/testing.yaml", gl);
    }

    public void render(GL3 gl){
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        lightPosition = getLightPosition();  // changing light position each frame

        Mat4 projectionMatrix = camera.getPerspectiveMatrix();
        Mat4 viewMatrix = camera.getViewMatrix();
        
        renderLight(gl, shaderLight, getLightModelMatrix(), viewMatrix, projectionMatrix);

        activeScene.render(gl, viewMatrix, projectionMatrix,
             lightAmbient, lightPosition, lightAmbient, lightDiffuse, lightSpecular);
        
        debugGrid.render(gl, viewMatrix, projectionMatrix);
    }

    private Vec3 getLightPosition() {
        double elapsedTime = getSeconds()-startTime;
        Vec3 lightPosition = new Vec3();
        lightPosition.x = 5.0f*(float)(Math.sin(Math.toRadians(elapsedTime*50)));
        lightPosition.y = 3.0f;
        lightPosition.z = 5.0f*(float)(Math.cos(Math.toRadians(elapsedTime*50)));
        return lightPosition;
    }

    private Mat4 getLightModelMatrix() {
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(0.3f,0.3f,0.3f), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(lightPosition), modelMatrix);
        return modelMatrix;
    }
    
    private void renderLight(GL3 gl, Shader shader, Mat4 modelMatrix, Mat4 view, Mat4 projection) {
        Mat4 mvpMatrix = Mat4.multiply(projection, Mat4.multiply(view, modelMatrix));
        
        shader.use(gl);
        shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

        // use diffuse value of light Material as colour appearance of light
        shader.setVec3(gl, "lightColor", lightDiffuse);

        light.render(gl);
    }

}