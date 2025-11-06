package engine.loaders;

import com.jogamp.opengl.GL3;
import engine.data.MaterialSpec;
import engine.gmaths.Vec3;
import engine.rendering.Material;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class MaterialLoader {
    public static Material Load(GL3 gl, String yamlPath) {
        try (InputStream input = Files.newInputStream(Paths.get(yamlPath))) {

            // Parse YAML into MaterialSpec
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(new Constructor(MaterialSpec.class, options));
            MaterialSpec spec = yaml.load(input);

            if (spec == null)
                throw new IllegalArgumentException("Failed to parse YAML: " + yamlPath);

            // Create Material instance
            Material material = new Material(gl, spec.vertex, spec.fragment);

            // Apply uniforms
            if (spec.uniforms != null) {
                for (Map.Entry<String, Object> entry : spec.uniforms.entrySet()) {
                    String name = entry.getKey();
                    Object value = entry.getValue();

                    // If it’s a list of numbers, convert to Vec3 or pass as list
                    if (value instanceof List<?>) {
                        List<?> list = (List<?>) value;
                        if (list.size() == 3 && allNumbers(list)) {
                            material.setUniform(name, new Vec3(
                                toFloat(list.get(0)),
                                toFloat(list.get(1)),
                                toFloat(list.get(2))
                            ));
                        } else {
                            material.setUniform(name, list);
                        }
                    } else if (value instanceof Number) {
                        material.setUniform(name, ((Number) value).floatValue());
                    } else {
                        // fallback: string or other
                        material.setUniform(name, value);
                    }
                }
            }

            // --- TEXTURES (not implemented yet) ---
            // if (spec.textures != null) { ... }

            material.apply(gl);
            return material;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading material: " + yamlPath, e);
        }
    }

    private static boolean allNumbers(List<?> list) {
        return list.stream().allMatch(o -> o instanceof Number);
    }

    private static float toFloat(Object o) {
        if (o instanceof Number) return ((Number) o).floatValue();
        return Float.parseFloat(o.toString());
    }
}
