package engine.loaders;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

/**
 * Generic YAML constructor that handles polymorphic deserialization
 * based on a discriminator field ("type").
 * 
 * Use this for ANY spec hierarchy that needs type-based routing:
 * - ComponentSpec → TransformSpec, MeshRendererSpec, etc.
 * - AssetSpec → TextureSpec, ModelSpec, AudioSpec, etc.
 * - BehaviorSpec → AISpec, PhysicsSpec, AnimationSpec, etc.
 */
public class PolymorphicYamlConstructor extends Constructor {
    private final Map<Class<?>, TypeRegistry<?>> registries = new HashMap<>();

    public PolymorphicYamlConstructor(Class<?> rootClass, LoaderOptions options) {
        super(rootClass, options);
    }

    /**
     * Register a polymorphic type hierarchy.
     * 
     * @param baseClass The base class (e.g., ComponentSpec.class)
     * @param discriminatorField The field used for routing (e.g., "type")
     * @param registry Map of discriminator values → concrete classes
     */
    public <T> void registerPolymorphicType(
            Class<T> baseClass,
            String discriminatorField,
            Map<String, Class<? extends T>> registry) {
        
        registries.put(baseClass, new TypeRegistry<>(discriminatorField, registry));
        System.out.println("[PolymorphicYamlConstructor] Registered " + 
            registry.size() + " variants for " + baseClass.getSimpleName());
    }

    @Override
    protected Object constructObject(Node node) {
        if (node instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) node;
            
            // Try to find a matching registry for this node
            for (Map.Entry<Class<?>, TypeRegistry<?>> entry : registries.entrySet()) {
                TypeRegistry<?> registry = entry.getValue();
                
                // Extract discriminator field value
                String discriminatorValue = extractField(mappingNode, registry.discriminatorField);
                
                if (discriminatorValue != null) {
                    // Look up the concrete class
                    Class<?> concreteClass = registry.typeMap.get(discriminatorValue);
                    
                    if (concreteClass != null) {
                        // Route to the correct subclass
                        node.setType(concreteClass);
                        System.out.println("[PolymorphicYamlConstructor] Routing '" + 
                            discriminatorValue + "' → " + concreteClass.getSimpleName());
                        break;
                    }
                }
            }
        }
        
        return super.constructObject(node);
    }

    /**
     * Extract a field value from a YAML mapping node
     */
    private String extractField(MappingNode node, String fieldName) {
        for (NodeTuple tuple : node.getValue()) {
            Node keyNode = tuple.getKeyNode();
            
            if (keyNode instanceof ScalarNode) {
                String key = ((ScalarNode) keyNode).getValue();
                
                if (fieldName.equals(key)) {
                    Node valueNode = tuple.getValueNode();
                    if (valueNode instanceof ScalarNode) {
                        return ((ScalarNode) valueNode).getValue();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Internal registry for a polymorphic type hierarchy
     */
    private static class TypeRegistry<T> {
        final String discriminatorField;
        final Map<String, Class<? extends T>> typeMap;
        
        TypeRegistry(String discriminatorField, Map<String, Class<? extends T>> typeMap) {
            this.discriminatorField = discriminatorField;
            this.typeMap = typeMap;
        }
    }
}
