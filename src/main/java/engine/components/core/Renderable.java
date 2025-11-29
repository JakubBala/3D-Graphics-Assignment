package engine.components.core;
import engine.components.Light;
import engine.gmaths.*;

import java.util.List;

import com.jogamp.opengl.*;

public interface Renderable {
    void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition, List<Light> lights);

    // void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition,
    //      Vec3 lightPosition, Vec3 lightAmbient, Vec3 lightDiffuse, Vec3 lightSpecular
    // );
}
