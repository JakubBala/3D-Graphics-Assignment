package assets.models;
/* I declare that this code is my own work */
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

public final class Plane {
  
    // ***************************************************
    /* THE DATA
    * A plane lying flat on the XZ plane (horizontal ground)
    * Normal points up (+Y)
    * Centered at origin
    * Size: 1x1 (from -0.5 to +0.5 on X and Z)
    */
    // anticlockwise/counterclockwise ordering
  
    public static final float[] vertices = new float[] {  // x,y,z, nx,ny,nz, s,t
        -0.5f, 0.0f, -0.5f,  0, 1, 0,  0.0f, 1.0f,  // 0 (back-left)
        -0.5f, 0.0f,  0.5f,  0, 1, 0,  0.0f, 0.0f,  // 1 (front-left)
        0.5f, 0.0f, -0.5f,  0, 1, 0,  1.0f, 1.0f,  // 2 (back-right)
        0.5f, 0.0f,  0.5f,  0, 1, 0,  1.0f, 0.0f   // 3 (front-right)
    };

    public static final int[] indices = new int[] {
        3, 2, 0,  // First triangle: front-right -> back-right -> back-left (CLOCKWISE)
        0, 1, 3   // Second triangle: back-left -> front-left -> front-right (CLOCKWISE)
    };
}
