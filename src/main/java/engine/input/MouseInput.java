package engine.input;

import java.awt.*;
import java.awt.event.*;
import engine.components.Camera;

public class MouseInput extends MouseMotionAdapter {
    private Point lastPoint;
    private final Camera camera;

    public MouseInput(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastPoint == null) return;
        Point ms = e.getPoint();
        float sensitivity = 0.001f;
        float dx = (float) (ms.x - lastPoint.x) * sensitivity;
        float dy = (float) (ms.y - lastPoint.y) * sensitivity;
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
            camera.updateYawPitch(dx, -dy);
        lastPoint = ms;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastPoint = e.getPoint();
    }
}
