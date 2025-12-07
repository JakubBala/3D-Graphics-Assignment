package engine.rendering;

import java.util.HashMap;
import java.util.Map;
import assets.models.*;

import com.jogamp.opengl.GL3;

public class MeshLibrary {

    private static final Map<String, Mesh> meshes = new HashMap<>();

    public static Mesh Load(String path, GL3 gl) {
        if (meshes.containsKey(path)) {
            return meshes.get(path); // return cached
        }

        // TODO: replace this with proper mesh file loading
        Mesh mesh = loadMeshFromFile(path, gl);

        meshes.put(path, mesh);
        return mesh;
    }

    private static Mesh loadMeshFromFile(String path, GL3 gl) {
        // Temporary implementation
        // Replace with real OBJ/mesh loader later.

        switch (path) {
            case "assets/models/Cube.mesh":
                return new Mesh(gl, Cube.vertices, Cube.indices);
            case "assets/models/Pyramid.mesh":
                return new Mesh(gl, Pyramid.vertices, Pyramid.indices);
            case "assets/models/Sphere.mesh":
                return new Mesh(gl, Sphere.vertices, Sphere.indices);
            case "assets/models/Plane.mesh":
                return new Mesh(gl, Plane.vertices, Plane.indices);
            case "assets/models/Cylinder.mesh":
                return new Mesh(gl, Cylinder.createVertices(1.0f), Cylinder.indices);
            case "assets/models/Cylinder5.mesh":
                return new Mesh(gl, Cylinder.createVertices(5.0f), Cylinder.indices);
            case "assets/models/Cylinder10.mesh":
                return new Mesh(gl, Cylinder.createVertices(10.0f), Cylinder.indices);

            // fallback:
            default:
                throw new RuntimeException("MeshLibrary: Cannot load mesh: " + path);
        }
    }
}
