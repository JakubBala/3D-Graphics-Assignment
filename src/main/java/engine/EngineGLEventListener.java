package engine;

import com.jogamp.opengl.*;

import assets.models.Cube;
import assets.models.Pyramid;
import assets.models.Sphere;
import engine.gmaths.*;
import engine.rendering.Mesh;
import engine.rendering.Shader;
import engine.components.Camera;
import engine.loaders.MaterialLoader;
import engine.loaders.SceneLoader;
import engine.rendering.Material;

public class EngineGLEventListener implements GLEventListener {

    private Shader shaderSphere, shaderLight;
    private final Camera camera;
    private double startTime;


    // TODO: This will go into GameObjects themselves
    private Mesh sphere;
    private Mesh light;

    private Vec3 cubeAmbient = new Vec3(1.0f, 0.5f, 0.31f);
    private Vec3 cubeDiffuse = new Vec3(1.0f, 0.5f, 0.31f);
    private Vec3 cubeSpecular = new Vec3(0.5f, 0.5f, 0.5f);
    private float cubeShininess = 32.0f;

    private Vec3 lightPosition = new Vec3(4f,5f,8f);
    private Vec3 lightAmbient = new Vec3(1.0f, 1.0f, 1.0f);
    private Vec3 lightDiffuse = new Vec3(0.0f, 0.0f, 0.0f);
    private Vec3 lightSpecular = new Vec3(0.0f, 0.0f, 0.0f);

    Material cubeMaterial;

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

        sphere = new Mesh(gl, Pyramid.vertices, Pyramid.indices);
        light = new Mesh(gl, Sphere.vertices, Sphere.indices);

        cubeMaterial = MaterialLoader.Load(gl, "assets/materials/lebron.yaml");
        
        shaderLight = new Shader(gl, "assets/shaders/vs_light_01.vert", "assets/shaders/fs_light_01.frag");
    }

    public void render(GL3 gl){
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        lightPosition = getLightPosition();  // changing light position each frame

        Mat4 projectionMatrix = camera.getPerspectiveMatrix();
        Mat4 viewMatrix = camera.getViewMatrix();
        
        //renderLight(gl, shaderLight, getLightModelMatrix(), viewMatrix, projectionMatrix);
        renderCube(gl, shaderSphere, getCubeModelMatrix(), viewMatrix, projectionMatrix);

        // TODO: Replace with scene.render(gl)
    }


    private Mat4 getCubeModelMatrix() {
        double elapsedTime = getSeconds() - startTime;
        float rotationAngle = (float)(elapsedTime * 100); // 50 degrees per second
        
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(10f, 10f, 10f), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(rotationAngle), modelMatrix);
        
        return modelMatrix;
    }

    // TODO: This would be done in Renderer.java
    private void renderCube(GL3 gl, Shader shader, Mat4 modelMatrix, Mat4 viewMatrix, Mat4 projectionMatrix) {
        
        cubeMaterial.useShader(gl);
        cubeMaterial.setTransformUniforms(gl, modelMatrix, viewMatrix, projectionMatrix, camera.getPosition());
        cubeMaterial.setLightUniforms(gl, lightPosition, lightAmbient, lightDiffuse, lightSpecular);
        cubeMaterial.apply(gl);

        sphere.render(gl);
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