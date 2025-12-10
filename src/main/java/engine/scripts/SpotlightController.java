package engine.scripts;

import engine.components.Behaviour;
import engine.components.Transform;
import engine.gmaths.Mat4;
import engine.gmaths.Mat4Transform;
import engine.gmaths.Vec3;


// This should be attached to the arm pivot
public class SpotlightController extends Behaviour{
    
    // SERIALIZED REFERENCES 
    public Transform target;
    public Transform light_pivot;

    // INTERNAL
    public Transform arm_pivot;
    private float circling_radius = 3f;
    private float circle_speed = 3f;
    @Override
    public void Start(){
        arm_pivot = getGameObject().getTransform();
    }

    @Override
    public void Update(){
        MoveSpotlightTarget();
        LookAtTarget();
    }

    private void LookAtTarget(){
        Vec3 targetPos = new Vec3(target.GetWorldPosition()); 

        Vec3 arm_pivot_w_pos = new Vec3(arm_pivot.GetWorldPosition());

        // Get the arm's forward direction in world space (before any rotation)
        Transform armParent = arm_pivot.getGameObject().getParent().getTransform();
        Mat4 parentWorld = armParent.getWorldMatrix();
        
        // Parents forward direction 
        Vec3 parentForward = new Vec3(
            -parentWorld.get(0, 2),
            -parentWorld.get(1, 2),
            -parentWorld.get(2, 2)
        );
        parentForward.normalize();
        
        // Project target direction onto the parents forward plane
        Vec3 toTarget = Vec3.subtract(arm_pivot_w_pos, targetPos);
        
        // Distance along parent's forward direction
        float forwardDist = Vec3.dotProduct(toTarget, parentForward);
        
        // Height difference
        float heightDiff = targetPos.y - arm_pivot_w_pos.y;
        
        // Calculate pitch
        float pitch = (float)Math.toDegrees(Math.atan2(heightDiff, forwardDist));
        
        arm_pivot.SetLocalRotation(pitch, 0f, 0f);


        // then for the light itself, 
        light_pivot.LookAt(
            targetPos,        
            new Vec3(0, 1, 0)// world up vector
        );

        System.err.println(arm_pivot.GetRotation());
    }


    // moves the spotlight target around the center (its parent) at a radius and speed
    private void MoveSpotlightTarget(){
        float time = (float)GameController.getElapsedTime();
        // convert real time to normalize 0 -> 1 cycle
        float normalized = (time % circle_speed) / circle_speed;

        // Convert normalized progress to radians
        float angle = normalized * (float)(2 * Math.PI);

        // Calculate orbit position (x,z)
        float x = (float) (Math.cos(angle) * circling_radius);
        float z = (float) (Math.sin(angle) * circling_radius);

        // Apply to the SpotlightTarget's transform
        Vec3 tPos = new Vec3(target.GetPosition());
        target.SetLocalPosition(x, tPos.y, z);

    }
}
