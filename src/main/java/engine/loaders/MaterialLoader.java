package engine.loaders;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;

import engine.data.MaterialSpec;
import engine.gmaths.Vec3;
import engine.rendering.Material;
import engine.rendering.TextureLibrary;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

            // --- UNIFORMS ---
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
                    } 
                    else if (value instanceof Number) {
                        Number num = (Number) value;
                        // Preserve integer values for uniform int; convert others to float
                        if (num instanceof Integer) {
                            material.setUniform(name, num.intValue());
                        } else {
                            material.setUniform(name, num.floatValue());
                        }
                    } else {
                        // fallback: string or other
                        material.setUniform(name, value);
                    }
                }
            }

            // --- TEXTURES ---
            Set<String> textureTypes = Set.of("albedo", "specular", "emission");

            if (spec.textures != null) {
                for (String type : textureTypes) {
                    String path = spec.textures.get(type);

                    if (path != null && !path.isEmpty()) {
                        Texture tex = TextureLibrary.LoadTexture(gl, path);
                        material.setTexture("material." + type + "Map", tex);
                        material.setUniform("material.has" + capitalize(type) + "Map", 1);
                    } else {
                        // No texture found, set flag to 0
                        material.setUniform("material.has" + capitalize(type) + "Map", 0);
                    }
                }
            } else {
                // No textures block in YAML — ensure all flags exist
                for (String type : textureTypes) {
                    material.setUniform("material.has" + capitalize(type) + "Map", 0);
                }
            }

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

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
