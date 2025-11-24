package engine.components;

import engine.components.core.Component;
import engine.gmaths.Mat4;
import engine.gmaths.Mat4Transform;
import engine.gmaths.Vec3;

public class Transform extends Component{
    private Vec3 local_position = new Vec3(0, 0, 0);
    private Vec3 local_rotation = new Vec3(0, 0, 0); // Euler angles
    private Vec3 local_scale = new Vec3(1, 1, 1);

    public Transform(Vec3 lp, Vec3 lr, Vec3 ls){
        local_position = lp;
        local_rotation = lr;
        local_scale = ls;
    }

    public Transform(){
        local_position = Vec3.zero;
        local_rotation = Vec3.zero;
        local_scale = Vec3.one;
    }

    public void SetLocalPosition(float x, float y, float z){
        local_position.x = x;
        local_position.y = y;
        local_position.z = z;
    }

    public void SetLocalRotation(float x, float y, float z){
        local_rotation.x = x;
        local_rotation.y = y;
        local_rotation.z = z;
    }

    public void SetLocalScale(float x, float y, float z){
        local_scale.x = x;
        local_scale.y = y;
        local_scale.z = z;
    }


    public Mat4 getModelMatrix() {
        Mat4 model = Mat4Transform.translate(local_position);
        // apply rotation (around x, y, z) and scale here
        // e.g.
        model = Mat4.multiply(model, Mat4Transform.rotateAroundX(local_rotation.x));
        model = Mat4.multiply(model, Mat4Transform.rotateAroundY(local_rotation.y));
        model = Mat4.multiply(model, Mat4Transform.rotateAroundZ(local_rotation.z));
        model = Mat4.multiply(model, Mat4Transform.scale(local_scale.x, local_scale.y, local_scale.z));
        return model;
    }
}
