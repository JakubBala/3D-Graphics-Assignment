package engine;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import engine.input.*;
import engine.components.Camera;

public class EngineWindow extends JFrame {
    private GLCanvas canvas;
    private EngineGLEventListener glListener;
    private final FPSAnimator animator;
    private final Camera camera;

    public EngineWindow(String title, int width, int height) {
        super(title);
        GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));

        canvas = new GLCanvas(glcapabilities);
        camera = new Camera(Camera.DEFAULT_POSITION, Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
        glListener = new EngineGLEventListener(camera);
        canvas.addGLEventListener(glListener);
        canvas.addMouseMotionListener(new MouseInput(camera));
        canvas.addKeyListener(new KeyboardInput(camera));

        add(canvas, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                animator.stop();
                remove(canvas);
                dispose();
                System.exit(0);
            }
        });
        animator = new FPSAnimator(canvas, 60);
        setPreferredSize(new Dimension(width, height));
    }

    public void run() {
        pack();
        setVisible(true);
        canvas.requestFocusInWindow();
        animator.start();
    }
}
