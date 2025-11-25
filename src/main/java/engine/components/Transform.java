package engine.components;

import engine.components.core.Component;
import engine.gmaths.Mat4;
import engine.gmaths.Mat4Transform;
import engine.gmaths.Vec3;
import engine.scene.GameObject;

public class Transform extends Component{
    // LOCAL space (relative to parent)
    private Vec3 local_position = new Vec3(0, 0, 0);
    private Vec3 local_rotation = new Vec3(0, 0, 0); // Euler angles
    private Vec3 local_scale = new Vec3(1, 1, 1);

    // WORLD space (cached, recalculated when dirty)
    private Mat4 worldMatrix;
    private boolean isDirty = true;

    public Transform(Vec3 lp, Vec3 lr, Vec3 ls){
        local_position = lp;
        local_rotation = lr;
        local_scale = ls;
    }

    public Transform(){
        local_position = Vec3.zero();
        local_rotation = Vec3.zero();
        local_scale = Vec3.one();
    }

     // Get the local TRS matrix
    public Mat4 getLocalMatrix() {
        Mat4 S = Mat4Transform.scale(local_scale.x, local_scale.y, local_scale.z);
        Mat4 R = Mat4.multiply(
            Mat4Transform.rotateAroundZ(local_rotation.z),
            Mat4.multiply(
                Mat4Transform.rotateAroundY(local_rotation.y),
                Mat4Transform.rotateAroundX(local_rotation.x)
            )
        );
        Mat4 T = Mat4Transform.translate(local_position);
        return Mat4.multiply(T, Mat4.multiply(R, S));
    }

    // Get the world matrix (with caching and dirty flagging)
    public Mat4 getWorldMatrix() {
        if (isDirty) {
            updateWorldMatrix();
        }
        return worldMatrix;
    }

    private void updateWorldMatrix() {
        Mat4 localMatrix = getLocalMatrix();
        
        GameObject parent = getGameObject().getParent();
        if (parent == null) {
            // No parent, local = world
            worldMatrix = localMatrix;
        } else {
            // World = Parent's World × Local
            Mat4 parentWorld = parent.getTransform().getWorldMatrix();
            worldMatrix = Mat4.multiply(parentWorld, localMatrix);
        }
        
        isDirty = false;
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

    public Vec3 GetPosition(){
        return local_position;
    }

    public Vec3 GetRotation(){
        return local_rotation;
    }

    public Vec3 GetScale(){
        return local_scale;
    }

    // Mark this transform and all children as dirty
    public void markDirty() {
        isDirty = true;
        // Recursively mark all children dirty
        for (GameObject child : gameObject.getChildren()) {
            child.getTransform().markDirty();
        }
    }
}
