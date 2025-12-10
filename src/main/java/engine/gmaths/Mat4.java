package engine.gmaths;

/**
 * A class for a 4x4 matrix.
 *
 * @author    Dr Steve Maddock
 * @version   1.0 (01/10/2017)
 */

/* Altered to add fully typed construct, extractRotationTranslation, toEulerXYZ*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class Mat4 {   // row column formulation

  private float[][] values;
  
  public Mat4() {
    this(0);
  }
  
  public Mat4(float f) {
    values = new float[4][4];
    makeZero();
    for (int i=0; i<4; ++i) {
      values[i][i] = f;
    }
  }


  
  public Mat4(Mat4 m) {
    this.values = new float[4][4];
    for (int i=0; i<4; ++i) {
      for (int j=0; j<4; ++j) {
        this.values[i][j] = m.values[i][j];
      }
    }
  }

  public Mat4(
    float m00, float m01, float m02, float m03,
    float m10, float m11, float m12, float m13,
    float m20, float m21, float m22, float m23,
    float m30, float m31, float m32, float m33
  ) {
      values = new float[4][4];

      values[0][0] = m00; values[0][1] = m01; values[0][2] = m02; values[0][3] = m03;
      values[1][0] = m10; values[1][1] = m11; values[1][2] = m12; values[1][3] = m13;
      values[2][0] = m20; values[2][1] = m21; values[2][2] = m22; values[2][3] = m23;
      values[3][0] = m30; values[3][1] = m31; values[3][2] = m32; values[3][3] = m33;
  }
  
  public void set(int r, int c, float f) {
    values[r][c] = f;
  }

  public float get(int r, int c) {
    return values[r][c];
  }
  
  private void makeZero() {
    for (int i=0; i<4; ++i) {
      for (int j=0; j<4; ++j) {
        values[i][j] = 0;
      }
    }
  }

  public static Mat4 identity(){
    return new Mat4(1);
  }
  public void transpose() {
    for (int i=0; i<4; ++i) {
      for (int j=i; j<4; ++j) {
        float t = values[i][j];
        values[i][j] = values[j][i];
        values[j][i] = t;
      }
    }
  }
    
  public static Mat4 transpose(Mat4 m) {
    Mat4 a = new Mat4(m);
    for (int i=0; i<4; ++i) {
      for (int j=i; j<4; ++j) {
        float t = a.values[i][j];
        a.values[i][j] = a.values[j][i];
        a.values[j][i] = t;
      }
    }
    return a;
  }

  public static Mat4 multiply(Mat4 a, Mat4 b) {
    Mat4 result = new Mat4();
    for (int i=0; i<4; ++i) {
      for (int j=0; j<4; ++j) {
        for (int k=0; k<4; ++k) {
          result.values[i][j] += a.values[i][k]*b.values[k][j];
        }
      }
    }
    return result;
  }
  
  public static Vec3 multiply(Mat4 m, Vec3 v) {
    Vec3 result = new Vec3();
    result.x = m.values[0][0]*v.x + m.values[0][1]*v.y
               + m.values[0][2]*v.z;
    result.y = m.values[1][0]*v.x + m.values[1][1]*v.y
               + m.values[1][2]*v.z;
    result.z = m.values[2][0]*v.x + m.values[2][1]*v.y
               + m.values[2][2]*v.z;
    return result;
  }

  public float[] toFloatArrayForGLSL() {  // col by row
    float[] f = new float[16];
    for (int j=0; j<4; ++j) {
      for (int i=0; i<4; ++i) {
        f[j*4+i] = values[i][j];
      }
    }
    return f;
  }
  
  public String asFloatArrayForGLSL() {  // col by row
    String s = "{";
    for (int j=0; j<4; ++j) {
      for (int i=0; i<4; ++i) {
        s += String.format("%.2f",values[i][j]);
        if (!(j==3 && i==3)) s+=",";
      }
    }
    return s;
  }
  
  public String toString() {
    String s = "{";
    for (int i=0; i<4; ++i) {
      s += (i==0) ? "{" : " {";
      for (int j=0; j<4; ++j) {
        s += String.format("%.2f",values[i][j]);  
        if (j<3) s += ", ";
      }
      s += (i==3) ? "}" : "},\n";
    } 
    s += "}";
    return s;
  }

  public static void extractRotationTranslation(Mat4 transform, Mat4 outRotation, Vec3 outTranslation) {
    // --- Extract Translation (row-major) ---
    outTranslation.x = transform.get(0, 3);
    outTranslation.y = transform.get(1, 3);
    outTranslation.z = transform.get(2, 3);

    // --- Extract Rotation (row-major 3x3) ---
    Vec3 row0 = new Vec3(transform.get(0, 0), transform.get(0, 1), transform.get(0, 2));
    Vec3 row1 = new Vec3(transform.get(1, 0), transform.get(1, 1), transform.get(1, 2));
    Vec3 row2 = new Vec3(transform.get(2, 0), transform.get(2, 1), transform.get(2, 2));

    // Normalize rows to remove scale
    row0.normalize();
    row1.normalize();
    row2.normalize();

    // Put rows back into a 4x4 rotation matrix
    outRotation.set(0, 0, row0.x); outRotation.set(0, 1, row0.y); outRotation.set(0, 2, row0.z); outRotation.set(0, 3, 0);
    outRotation.set(1, 0, row1.x); outRotation.set(1, 1, row1.y); outRotation.set(1, 2, row1.z); outRotation.set(1, 3, 0);
    outRotation.set(2, 0, row2.x); outRotation.set(2, 1, row2.y); outRotation.set(2, 2, row2.z); outRotation.set(2, 3, 0);

    outRotation.set(3, 0, 0);
    outRotation.set(3, 1, 0);
    outRotation.set(3, 2, 0);
    outRotation.set(3, 3, 1);
  }

  public Vec3 toEulerXYZ() {
    // Read rotation matrix entries
    float m00 = get(0,0);
    float m01 = get(0,1);
    float m02 = get(0,2);

    float m10 = get(1,0);
    float m11 = get(1,1);
    float m12 = get(1,2);

    float m20 = get(2,0);
    float m21 = get(2,1);
    float m22 = get(2,2);

    float pitch;  // x
    float yaw;    // y
    float roll;   // z

    // pitch (X-axis)
    pitch = (float)Math.asin(-m21);

    // Check gimbal lock
    if (Math.abs(m21) < 0.999999f) {

        // yaw (Y-axis)
        yaw = (float)Math.atan2(m20, m22);

        // roll (Z-axis)
        roll = (float)Math.atan2(m01, m11);

    } else {
        // Gimbal lock fallback
        yaw = 0;
        roll = (float)Math.atan2(-m10, m00);
    }

    return new Vec3(
        (float)Math.toDegrees(pitch),
        (float)Math.toDegrees(yaw),
        (float)Math.toDegrees(roll)
    );
  }
  
} // end of Mat4 class