package engine.loaders;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import com.jogamp.opengl.GL3;

import engine.data.GameObjectSpec;
import engine.data.SceneSpec;
import engine.scene.Scene;
import engine.scene.GameObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;

public class SceneLoader {

    public static Scene Load(String yamlPath, GL3 gl) {

        try (InputStream in = Files.newInputStream(Paths.get(yamlPath))) {

            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(new Constructor(SceneSpec.class, options));
            SceneSpec spec = yaml.load(in);
            
            Scene scene = new Scene(spec.name);

            System.out.println("[SceneLoader] Instantiated Scene: " + scene.getName());

            for (GameObjectSpec gameObjectSpec : spec.gameObjects) {
                GameObject loadedGameObject = GameObjectLoader.Load(gameObjectSpec, gl);
                scene.AddGameObject(loadedGameObject);
                System.out.println("[SceneLoader] Added GameObject: " + loadedGameObject.getName());
            }

            return scene;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load scene: " + yamlPath, e);
        }
    }
}
