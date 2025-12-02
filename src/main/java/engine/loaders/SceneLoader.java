package engine.loaders;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import com.jogamp.opengl.GL3;

import engine.data.GameObjectSpec;
import engine.data.SceneSpec;
import engine.rendering.Material;
import engine.scene.Scene;
import engine.scene.Skybox;
import engine.scene.GameObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;

public class SceneLoader {

    public static Scene Load(String yamlPath, GL3 gl) {

        try (InputStream in = Files.newInputStream(Paths.get(yamlPath))) {

            LoaderOptions options = new LoaderOptions();
            PolymorphicYamlConstructor constructor = new PolymorphicYamlConstructor(
                SceneSpec.class, options);
            SpecRegistry.registerAllTypes(constructor);

            Yaml yaml = new Yaml(constructor);
            SceneSpec spec = yaml.load(in);

            if (spec == null) {
                throw new IllegalArgumentException("Failed to parse YAML: " + yamlPath);
            }
            
            Scene scene = new Scene(spec.name);

            System.out.println("[SceneLoader] Instantiated Scene: " + scene.getName());

            // Load GameObjects (ComponentSpecs are already deserialized and typed)
            for (GameObjectSpec gameObjectSpec : spec.gameObjects) {
                GameObject loadedGameObject = GameObjectLoader.Load(gameObjectSpec, gl);
                scene.AddGameObject(loadedGameObject);
                System.out.println("[SceneLoader] Added GameObject: " + loadedGameObject.getName());
            }

            // Load Skybox (if specified)
            if (spec.skybox != null && !spec.skybox.isEmpty()) {
                Material skyMaterial = MaterialLoader.Load(gl, spec.skybox);
                Skybox skybox = new Skybox(gl, skyMaterial);
                scene.SetSkybox(skybox);
                System.out.println("[SceneLoader] Loaded Skybox: " + spec.skybox);
            }

            return scene;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load scene: " + yamlPath, e);
        }
    }
}
