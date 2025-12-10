package engine.scripts;

import engine.components.Behaviour;
import engine.components.Transform;
import engine.gmaths.Vec3;


// This should be attached to the arm pivot
public class SpotlightController extends Behaviour{
    
    // SERIALIZED REFERENCES 
    public Transform target;
    public Transform light_pivot;

    // INTERNAL
    public Transform arm_pivot;

    @Override
    public void Start(){
        arm_pivot = getGameObject().getTransform();
    }

    @Override
    public void Update(){

        Vec3 targetPos = new Vec3(target.GetWorldPosition()); 

        Vec3 arm_pivot_w_pos = new Vec3(arm_pivot.GetWorldPosition());
        Vec3 arm_pivot_target = new Vec3(targetPos.x, arm_pivot_w_pos.y, targetPos.z);

        // for the arm pivot, we only want it to rotate on the X so we set a virtual target
        // at the same Y level as it.
        arm_pivot.LookAt(
            arm_pivot_target,        
            new Vec3(0, 1, 0)// world up vector
        );


        // then for the light itself, 
        light_pivot.LookAt(
            targetPos,        
            new Vec3(0, 1, 0)// world up vector
        );
    }
}
