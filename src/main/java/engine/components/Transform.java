package engine.components;

import com.jogamp.opengl.GL3;

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
    private boolean debugAxesEnabled = false;

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
            // World = Parent's World x Local
            Mat4 parentWorld = parent.getTransform().getWorldMatrix();
            worldMatrix = Mat4.multiply(parentWorld, localMatrix);
        }
        
        isDirty = false;
    }

    public void SetLocalPosition(float x, float y, float z){
        local_position.x = x;
        local_position.y = y;
        local_position.z = z;
        markDirty();
    }

    public void SetLocalRotation(float x, float y, float z){
        local_rotation.x = x;
        local_rotation.y = y;
        local_rotation.z = z;
        markDirty();
    }

    public void SetLocalScale(float x, float y, float z){
        local_scale.x = x;
        local_scale.y = y;
        local_scale.z = z;
        markDirty();
    }

    public void enableDebugAxes() {
        if (!debugAxesEnabled) {
            debugAxesEnabled = true;
        }
    }

    public boolean isDebugAxesEnabled() {
        return debugAxesEnabled;
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
    
    public Vec3 GetWorldPosition(){
        Mat4 world = getWorldMatrix();
        return new Vec3(world.get(0,3), world.get(1,3), world.get(2,3));
    }

    // Direction vectors in WORLD space
    // Forward is considered to be -Z in local space
    public Vec3 GetForward() {
        Mat4 world = getWorldMatrix();

        // Forward column (Z axis)
        Vec3 f = new Vec3(
            world.get(0, 2),
            world.get(1, 2),
            world.get(2, 2)
        );

        // Forward is -Z in coordinate system:
        f = Vec3.multiply(f, -1f);

        return Vec3.normalize(f);
    }

    public Vec3 GetRight() {
        Mat4 world = getWorldMatrix();

        Vec3 r = new Vec3(
            world.get(0, 0),
            world.get(1, 0),
            world.get(2, 0)
        );

        return Vec3.normalize(r);
    }

    public Vec3 GetUp() {
        Mat4 world = getWorldMatrix();

        Vec3 u = new Vec3(
            world.get(0, 1),
            world.get(1, 1),
            world.get(2, 1)
        );

        return Vec3.normalize(u);
    }

    public void Translate(Vec3 delta) {
        local_position = Vec3.add(local_position, delta);
        markDirty();
    }

    // Rotate local Euler angles (degrees) around local axes and mark dirty
    public void RotateLocalX(float degrees) {
        local_rotation.x += degrees;
        markDirty();
    }

    public void RotateLocalY(float degrees) {
        local_rotation.y += degrees;
        markDirty();
    }

    public void RotateLocalZ(float degrees) {
        local_rotation.z += degrees;
        markDirty();
    }

    public void LookAt(Vec3 worldTarget, Vec3 worldUp) {
        // 1. Get world position of this object
        Vec3 worldPos = GetWorldPosition();

        // 2. Compute desired forward direction (world space)
        Vec3 worldDir = Vec3.subtract(worldTarget, worldPos);
        if (worldDir.length() < 0.0001f) return;   // Prevent NaN
        worldDir.normalize();

        // 3. Convert world direction into local space
        Mat4 parentWorld =
                (getGameObject().getParent() != null)
                ? getGameObject().getParent().getTransform().getWorldMatrix()
                : Mat4.identity();

        Mat4 parentInv = Mat4Transform.inverse(parentWorld);

        // Direction -> vec4 with w = 0
        Vec3 localDir = Mat4Transform.multiplyDirection(parentInv, worldDir);
        localDir.normalize();

        // 4. engine uses forward = -Z, so invert
        Vec3 f = localDir;  // f = desired local forward

        // 5. Compute yaw and pitch for rotation order (Rz * Ry * Rx)
        // Yaw 
        float yaw = (float)Math.toDegrees(Math.atan2(f.x, -f.z));

        // Pitch 
        // angle between forward and XZ plane
        float pitch = (float)Math.toDegrees(
                Math.atan2(f.y, Math.sqrt(f.x * f.x + f.z * f.z))
        );

        float roll = 0f;

        // 6. Apply result
        SetLocalRotation(pitch, yaw, roll);
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
