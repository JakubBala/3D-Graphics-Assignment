package engine.input;

import java.awt.event.*;
import engine.components.Camera;

public class KeyboardInput extends KeyAdapter {
    private final Camera camera;

    public KeyboardInput(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Camera.Movement m = Camera.Movement.NO_MOVEMENT;

        if (e.isShiftDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W -> m = Camera.Movement.FAST_FORWARD;
                case KeyEvent.VK_S -> m = Camera.Movement.FAST_BACK;
                case KeyEvent.VK_A -> m = Camera.Movement.FAST_LEFT;
                case KeyEvent.VK_D -> m = Camera.Movement.FAST_RIGHT;
                case KeyEvent.VK_Q -> m = Camera.Movement.FAST_UP;
                case KeyEvent.VK_E -> m = Camera.Movement.FAST_DOWN;
            }
        } else {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W -> m = Camera.Movement.FORWARD;
                case KeyEvent.VK_S -> m = Camera.Movement.BACK;
                case KeyEvent.VK_A -> m = Camera.Movement.LEFT;
                case KeyEvent.VK_D -> m = Camera.Movement.RIGHT;
                case KeyEvent.VK_Q -> m = Camera.Movement.UP;
                case KeyEvent.VK_E -> m = Camera.Movement.DOWN;
            }
        }

        camera.keyboardInput(m);
    }
}
