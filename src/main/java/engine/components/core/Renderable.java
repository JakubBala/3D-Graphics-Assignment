package engine.components.core;
import engine.components.Light;
import engine.gmaths.*;
/* I declare that this code is my own work */
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/


import java.util.List;

import com.jogamp.opengl.*;

public interface Renderable {
    void render(GL3 gl, Mat4 view, Mat4 projection, Vec3 cameraPosition, List<Light> lights);
}
