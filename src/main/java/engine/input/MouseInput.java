package engine.input;

import java.awt.*;
import java.awt.event.*;

import engine.EngineGLEventListener;
import engine.components.Camera;
/* I declare that this code is my own work, adapted from Lab Code*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class MouseInput extends MouseMotionAdapter {
    private Point lastPoint;
    private final EngineGLEventListener context;

    public MouseInput(EngineGLEventListener context) {
        this.context = context;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastPoint == null) return;
        Point ms = e.getPoint();
        float sensitivity = 0.001f;
        float dx = (float) (ms.x - lastPoint.x) * sensitivity;
        float dy = (float) (ms.y - lastPoint.y) * sensitivity;
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
            context.mouseInput(dx, -dy);
        lastPoint = ms;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastPoint = e.getPoint();
    }
}
