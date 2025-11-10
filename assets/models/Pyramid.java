package assets.models;

public final class Pyramid {
  
  // ***************************************************
  /* THE DATA
   * Square base pyramid with apex at (0, 0.5, 0) and base at y = -0.5
   * Base vertices at corners: (-0.5, -0.5, -0.5), (0.5, -0.5, -0.5), etc.
   */
  // anticlockwise/counterclockwise ordering
  
   public static final float[] vertices = new float[] {  // x,y,z, nx,ny,nz, s,t
      // Apex vertex (duplicated for each face with different normals)
      0.0f,  0.5f,  0.0f,  0, 0.4472f, -0.8944f,  0.5f, 1.0f,  // 0 - apex for front face
      0.0f,  0.5f,  0.0f,  0.8944f, 0.4472f, 0,  0.5f, 1.0f,  // 1 - apex for right face
      0.0f,  0.5f,  0.0f,  0, 0.4472f, 0.8944f,  0.5f, 1.0f,  // 2 - apex for back face
      0.0f,  0.5f,  0.0f,  -0.8944f, 0.4472f, 0,  0.5f, 1.0f,  // 3 - apex for left face
      
      // Base vertices - front face (z -ve)
      -0.5f, -0.5f, -0.5f,  0, 0.4472f, -0.8944f,  0.0f, 0.0f,  // 4
       0.5f, -0.5f, -0.5f,  0, 0.4472f, -0.8944f,  1.0f, 0.0f,  // 5
      
      // Base vertices - right face (x +ve)
       0.5f, -0.5f, -0.5f,  0.8944f, 0.4472f, 0,  0.0f, 0.0f,  // 6
       0.5f, -0.5f,  0.5f,  0.8944f, 0.4472f, 0,  1.0f, 0.0f,  // 7
      
      // Base vertices - back face (z +ve)
       0.5f, -0.5f,  0.5f,  0, 0.4472f, 0.8944f,  0.0f, 0.0f,  // 8
      -0.5f, -0.5f,  0.5f,  0, 0.4472f, 0.8944f,  1.0f, 0.0f,  // 9
      
      // Base vertices - left face (x -ve)
      -0.5f, -0.5f,  0.5f,  -0.8944f, 0.4472f, 0,  0.0f, 0.0f,  // 10
      -0.5f, -0.5f, -0.5f,  -0.8944f, 0.4472f, 0,  1.0f, 0.0f,  // 11
      
      // Base vertices - bottom face (y -ve)
      -0.5f, -0.5f, -0.5f,  0, -1, 0,  0.0f, 0.0f,  // 12
       0.5f, -0.5f, -0.5f,  0, -1, 0,  1.0f, 0.0f,  // 13
       0.5f, -0.5f,  0.5f,  0, -1, 0,  1.0f, 1.0f,  // 14
      -0.5f, -0.5f,  0.5f,  0, -1, 0,  0.0f, 1.0f   // 15
   };
     
   public static final int[] indices = new int[] {
      // Front face (z -ve) - counterclockwise from outside
      0, 5, 4,
      
      // Right face (x +ve) - counterclockwise from outside
      1, 7, 6,
      
      // Back face (z +ve) - counterclockwise from outside
      2, 9, 8,
      
      // Left face (x -ve) - counterclockwise from outside
      3, 11, 10,
      
      // Bottom face (y -ve) - counterclockwise from outside (looking up from below)
      12, 15, 14,
      14, 13, 12
  };

}