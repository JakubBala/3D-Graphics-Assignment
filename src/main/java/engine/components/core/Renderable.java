package engine.components.core;
import engine.gmaths.*;
import com.jogamp.opengl.*;

public interface Renderable {
    void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition,
         Vec3 lightPosition, Vec3 lightAmbient, Vec3 lightDiffuse, Vec3 lightSpecular
    );
}
