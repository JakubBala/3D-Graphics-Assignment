package engine.loaders;

import engine.data.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all polymorphic spec types in the engine.
 * Add new spec hierarchies here as your engine grows.
 */
public class SpecRegistry {
    
    /**
     * Configure a YAML constructor with all known polymorphic types
     */
    public static void registerAllTypes(PolymorphicYamlConstructor constructor) {
        registerComponentSpecs(constructor);
    }
    
    /**
     * Register all Component spec variants
     */
    private static void registerComponentSpecs(PolymorphicYamlConstructor constructor) {
        Map<String, Class<? extends ComponentSpec>> componentTypes = new HashMap<>();
        
        // Core components
        componentTypes.put("Transform", TransformSpec.class);
        componentTypes.put("MeshRenderer", MeshRendererSpec.class);
        componentTypes.put("Light", LightSpec.class);
        
        // Future components -> add here:
        // componentTypes.put("Camera", CameraSpec.class);
        
        constructor.registerPolymorphicType(
            ComponentSpec.class,
            "type",  // discriminator field
            componentTypes
        );
    }
}
