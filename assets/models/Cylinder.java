package assets.models;

/* I declare that this code is my own work, with the help of Claude */
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

public class Cylinder {
  // ***************************************************
  /* THE DATA
   */
  // anticlockwise/counterclockwise ordering
 
  private static final int SEGMENTS = 12; // Number of segments around the circle
  
  //public static final float[] vertices = createVertices();
  public static final int[] indices = createIndices();

  public static float[] createVertices(float height) {
    float radius = 0.5f;
    float halfHeight = height / 2.0f;
    
    int step = 8; // x, y, z, nx, ny, nz, s, t
    
    // Vertices breakdown:
    // - Side surface: (SEGMENTS + 1) vertices x 2 rows (top and bottom)
    // - Top cap: SEGMENTS + 2 (center + perimeter + closing vertex)
    // - Bottom cap: SEGMENTS + 2 (center + perimeter + closing vertex)
    
    int sideVertCount = (SEGMENTS + 1) * 2;
    int capVertCount = SEGMENTS + 2;
    int totalVerts = sideVertCount + capVertCount * 2;
    
    float[] vertices = new float[totalVerts * step];
    int vertIndex = 0;
    
    // === SIDE SURFACE ===
    // Bottom ring
    for (int i = 0; i <= SEGMENTS; i++) {
      float u = (float) i / SEGMENTS;
      float angle = (float) (2.0 * Math.PI * u);
      
      float x = (float) (radius * Math.cos(angle));
      float z = (float) (radius * Math.sin(angle));
      
      // Normal points radially outward
      float nx = (float) Math.cos(angle);
      float nz = (float) Math.sin(angle);
      
      int base = vertIndex * step;
      vertices[base + 0] = x;
      vertices[base + 1] = -halfHeight;
      vertices[base + 2] = z;
      vertices[base + 3] = nx;
      vertices[base + 4] = 0.0f; // Normal Y = 0 for sides
      vertices[base + 5] = nz;
      vertices[base + 6] = u;
      vertices[base + 7] = 0.0f; // V = 0 at bottom
      
      vertIndex++;
    }
    
    // Top ring
    for (int i = 0; i <= SEGMENTS; i++) {
      float u = (float) i / SEGMENTS;
      float angle = (float) (2.0 * Math.PI * u);
      
      float x = (float) (radius * Math.cos(angle));
      float z = (float) (radius * Math.sin(angle));
      
      float nx = (float) Math.cos(angle);
      float nz = (float) Math.sin(angle);
      
      int base = vertIndex * step;
      vertices[base + 0] = x;
      vertices[base + 1] = halfHeight;
      vertices[base + 2] = z;
      vertices[base + 3] = nx;
      vertices[base + 4] = 0.0f;
      vertices[base + 5] = nz;
      vertices[base + 6] = u;
      vertices[base + 7] = 1.0f; // V = 1 at top
      
      vertIndex++;
    }
    
    int sideVertEnd = vertIndex;
    
    // === TOP CAP (y = +halfHeight, normal = +Y) ===
    // Center vertex
    int topCenterIndex = vertIndex;
    int base = vertIndex * step;
    vertices[base + 0] = 0.0f;
    vertices[base + 1] = halfHeight;
    vertices[base + 2] = 0.0f;
    vertices[base + 3] = 0.0f;
    vertices[base + 4] = 1.0f; // Normal pointing up
    vertices[base + 5] = 0.0f;
    vertices[base + 6] = 0.5f; // UV center
    vertices[base + 7] = 0.5f;
    vertIndex++;
    
    // Perimeter vertices
    for (int i = 0; i <= SEGMENTS; i++) {
      float u = (float) i / SEGMENTS;
      float angle = (float) (2.0 * Math.PI * u);
      
      float x = (float) (radius * Math.cos(angle));
      float z = (float) (radius * Math.sin(angle));
      
      base = vertIndex * step;
      vertices[base + 0] = x;
      vertices[base + 1] = halfHeight;
      vertices[base + 2] = z;
      vertices[base + 3] = 0.0f;
      vertices[base + 4] = 1.0f; // Normal up
      vertices[base + 5] = 0.0f;
      vertices[base + 6] = 0.5f + 0.5f * (float) Math.cos(angle);
      vertices[base + 7] = 0.5f + 0.5f * (float) Math.sin(angle);
      
      vertIndex++;
    }
    
    // === BOTTOM CAP (y = -halfHeight, normal = -Y) ===
    // Center vertex
    int bottomCenterIndex = vertIndex;
    base = vertIndex * step;
    vertices[base + 0] = 0.0f;
    vertices[base + 1] = -halfHeight;
    vertices[base + 2] = 0.0f;
    vertices[base + 3] = 0.0f;
    vertices[base + 4] = -1.0f; // Normal pointing down
    vertices[base + 5] = 0.0f;
    vertices[base + 6] = 0.5f; // UV center
    vertices[base + 7] = 0.5f;
    vertIndex++;
    
    // Perimeter vertices
    for (int i = 0; i <= SEGMENTS; i++) {
      float u = (float) i / SEGMENTS;
      float angle = (float) (2.0 * Math.PI * u);
      
      float x = (float) (radius * Math.cos(angle));
      float z = (float) (radius * Math.sin(angle));
      
      base = vertIndex * step;
      vertices[base + 0] = x;
      vertices[base + 1] = -halfHeight;
      vertices[base + 2] = z;
      vertices[base + 3] = 0.0f;
      vertices[base + 4] = -1.0f; // Normal down
      vertices[base + 5] = 0.0f;
      vertices[base + 6] = 0.5f + 0.5f * (float) Math.cos(angle);
      vertices[base + 7] = 0.5f - 0.5f * (float) Math.sin(angle); // Inverted for correct winding
      
      vertIndex++;
    }
    
    return vertices;
  }
  
  private static int[] createIndices() {
    // Side surface: SEGMENTS quads x 2 triangles x 3 indices
    int sideIndices = SEGMENTS * 2 * 3;
    
    // Each cap: SEGMENTS triangles x 3 indices
    int capIndices = SEGMENTS * 3 * 2;
    
    int[] indices = new int[sideIndices + capIndices];
    int idx = 0;
    
    // === SIDE SURFACE INDICES (CORRECTED) ===
    int bottomRingStart = 0;
    int topRingStart = SEGMENTS + 1;
    
    for (int i = 0; i < SEGMENTS; i++) {
      // First triangle (CCW when viewed from outside)
      indices[idx++] = bottomRingStart + i;
      indices[idx++] = topRingStart + i;
      indices[idx++] = topRingStart + i + 1;
      
      // Second triangle (CCW when viewed from outside)
      indices[idx++] = bottomRingStart + i;
      indices[idx++] = topRingStart + i + 1;
      indices[idx++] = bottomRingStart + i + 1;
    }
    
    // === TOP CAP INDICES (triangle fan) ===
    int sideVertCount = (SEGMENTS + 1) * 2;
    int topCenterIndex = sideVertCount;
    int topRingFirstVertex = topCenterIndex + 1;
    
    for (int i = 0; i < SEGMENTS; i++) {
      indices[idx++] = topCenterIndex;
      indices[idx++] = topRingFirstVertex + i + 1;
      indices[idx++] = topRingFirstVertex + i;
    }
    
    // === BOTTOM CAP INDICES (triangle fan) ===
    int bottomCenterIndex = topRingFirstVertex + SEGMENTS + 1;
    int bottomRingFirstVertex = bottomCenterIndex + 1;
    
    for (int i = 0; i < SEGMENTS; i++) {
      indices[idx++] = bottomCenterIndex;
      indices[idx++] = bottomRingFirstVertex + i;
      indices[idx++] = bottomRingFirstVertex + i + 1;
    }
    
    return indices;
  }
}
