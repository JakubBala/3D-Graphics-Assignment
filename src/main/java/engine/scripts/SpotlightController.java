package engine.scripts;

import engine.components.Behaviour;
import engine.components.Transform;


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

    }
}
