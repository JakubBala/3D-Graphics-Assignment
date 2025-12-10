package engine;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import engine.input.*;
import engine.components.Camera;

public class EngineWindow extends JFrame implements ActionListener{
    private GLCanvas canvas;
    private EngineGLEventListener glListener;
    private final FPSAnimator animator;

    // UI components
    private JSlider globalLightSlider;
    private JSlider spotlightLightSlider;
    private JCheckBox spotlightMotionToggle;
    private JButton poseModeButton;
    private JButton motionModeButton;
    private JButton keyPose1Button;
    private JButton keyPose2Button;
    private JButton keyPose3Button;

    private boolean isPoseMode = true;

    private JPanel controlPanel;      // Parent panel containing all controls
    private JPanel poseButtonsPanel;  // Panel containing the key pose buttons

    // Constants for layout
    private static final int SIDEBAR_WIDTH = 280;

    public EngineWindow(String title, int width, int height) {
        super(title);
        GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));

        // --- Canvas Setup ---
        canvas = new GLCanvas(glcapabilities);
        glListener = new EngineGLEventListener();
        canvas.addGLEventListener(glListener);
        canvas.addMouseMotionListener(new MouseInput(glListener));
        canvas.addKeyListener(new KeyboardInput(glListener));
        
        // Add Canvas to Center (it will take up all remaining space)
        add(canvas, BorderLayout.CENTER);

        // --- Control Panel Setup ---
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, height));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around edges

        // 1. SECTION: LIGHTING
        JLabel lightingHeader = new JLabel("--- Lighting Controls ---");
        lightingHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(lightingHeader);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Global Light Slider
        JPanel globalLightPanel = createSliderPanel("Global Light Strength");
        globalLightSlider = new JSlider(0, 100, 100);
        globalLightSlider.addChangeListener(e -> onGlobalLightStrengthChanged(globalLightSlider.getValue() / 100f));
        globalLightPanel.add(globalLightSlider, BorderLayout.CENTER);
        controlPanel.add(globalLightPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Spotlight Slider
        JPanel spotlightLightPanel = createSliderPanel("Spotlight Light Strength");
        spotlightLightSlider = new JSlider(0, 100, 100);
        spotlightLightSlider.addChangeListener(e -> onSpotlightLightStrengthChanged(spotlightLightSlider.getValue() / 100f));
        spotlightLightPanel.add(spotlightLightSlider, BorderLayout.CENTER);
        controlPanel.add(spotlightLightPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Spotlight Toggle
        spotlightMotionToggle = new JCheckBox("Spotlight Motion");
        spotlightMotionToggle.setSelected(true);
        spotlightMotionToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        spotlightMotionToggle.addItemListener(e -> onSpotlightMotionToggled(e.getStateChange() == ItemEvent.SELECTED));
        controlPanel.add(spotlightMotionToggle);

        // Separator
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. SECTION: ANIMATION MODES
        JLabel modeHeader = new JLabel("--- Animation Mode ---");
        modeHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(modeHeader);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        poseModeButton = new JButton("Pose Mode");
        motionModeButton = new JButton("Continuous Motion Mode");
        
        // Align buttons to center
        poseModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        motionModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        poseModeButton.addActionListener(this);
        motionModeButton.addActionListener(this);

        controlPanel.add(poseModeButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        controlPanel.add(motionModeButton);

        // Separator
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. SECTION: POSE SELECTION
        poseButtonsPanel = new JPanel();
        poseButtonsPanel.setLayout(new BoxLayout(poseButtonsPanel, BoxLayout.Y_AXIS));
        poseButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel poseHeader = new JLabel("--- Key Poses ---");
        poseHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        poseButtonsPanel.add(poseHeader);
        poseButtonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        keyPose1Button = new JButton("Pose 1");
        keyPose2Button = new JButton("Pose 2");
        keyPose3Button = new JButton("Pose 3");

        // Center align the pose buttons
        keyPose1Button.setAlignmentX(Component.CENTER_ALIGNMENT);
        keyPose2Button.setAlignmentX(Component.CENTER_ALIGNMENT);
        keyPose3Button.setAlignmentX(Component.CENTER_ALIGNMENT);

        keyPose1Button.addActionListener(this);
        keyPose2Button.addActionListener(this);
        keyPose3Button.addActionListener(this);

        poseButtonsPanel.add(keyPose1Button);
        poseButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        poseButtonsPanel.add(keyPose2Button);
        poseButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        poseButtonsPanel.add(keyPose3Button);

        controlPanel.add(poseButtonsPanel);
        poseButtonsPanel.setVisible(isPoseMode);

        // Add Glue to push everything to the top (prevents vertical spreading if window is tall)
        controlPanel.add(Box.createVerticalGlue());

        // Add Control Panel to the Right (East)
        add(controlPanel, BorderLayout.EAST);

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

    /**
     * Helper to create a standardized slider panel with a label on top
     */
    private JPanel createSliderPanel(String labelText) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, BorderLayout.NORTH);
        panel.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 60)); // Limit height
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "Pose Mode":
                isPoseMode = true;
                System.out.println("Switched to Pose Mode");
                poseButtonsPanel.setVisible(true);
                controlPanel.revalidate();  // update layout
                controlPanel.repaint();
                break;

            case "Continuous Motion Mode":
                isPoseMode = false;
                System.out.println("Switched to Continuous Motion Mode");
                poseButtonsPanel.setVisible(false);
                controlPanel.revalidate();  // update layout
                controlPanel.repaint();
                break;

            case "Pose 1":
                if (isPoseMode) {
                    System.out.println("Switching to Pose 1");
                    onSwitchToPose(1);
                }
                break;

            case "Pose 2":
                if (isPoseMode) {
                    System.out.println("Switching to Pose 2");
                    onSwitchToPose(2);
                }
                break;

            case "Pose 3":
                if (isPoseMode) {
                    System.out.println("Switching to Pose 3");
                    onSwitchToPose(3);
                }
                break;
        }
    }

    private void onGlobalLightStrengthChanged(float value) {
        System.out.println("Global Light Strength: " + value);
        // TODO: call your engine function here
    }

    private void onSpotlightLightStrengthChanged(float value) {
        System.out.println("Spotlight Light Strength: " + value);
        // TODO: call your engine function here
    }

    private void onSpotlightMotionToggled(boolean enabled) {
        System.out.println("Spotlight Motion toggled: " + enabled);
        // TODO: call your engine function here
    }

    private void onSwitchToPose(int poseNumber) {
        System.out.println("Switched to Pose " + poseNumber);
        // TODO: implement switching bee pose here
    }

    public void run() {
        pack();
        setVisible(true);
        canvas.requestFocusInWindow();
        animator.start();
    }
}
