package engine.data;

import engine.components.Behaviour;
import engine.components.core.Component;
import engine.gmaths.Vec2;
import engine.gmaths.Vec3;
import engine.scene.GameObject;
import com.jogamp.opengl.GL3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

/**
 * Generic spec for all Behaviour scripts.
 * Dynamically instantiates the script class and populates its fields.
 */
public class BehaviourSpec extends ComponentSpec {
    
    // The script class name (e.g., "Rotator", "CameraPan")
    public String script;
    
    // All other YAML fields go here as a map
    public Map<String, Object> properties;

    // References to other objects/components by ID
    public Map<String, String> refs; 
    
    @Override
    public Component createComponent(GameObject gameObject, GL3 gl) {
        try {
            // Construct full class name (needs to include package)
            String className = "engine.scripts." + script;
            
            // Load and instantiate the class
            Class<?> scriptClass = Class.forName(className);
            Behaviour behaviour = (Behaviour) scriptClass.getDeclaredConstructor().newInstance();
            
            // Populate fields from YAML properties
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String fieldName = entry.getKey();
                    Object value = entry.getValue();
                    
                    try {
                        // only public fields
                        Field field = scriptClass.getField(fieldName);
                        field.setAccessible(true);
                        
                        // Handle type conversion
                        Object convertedValue = convertValue(value, field);
                        field.set(behaviour, convertedValue);
                        
                        System.out.println("[BehaviourSpec]: Set " + fieldName + " = " + convertedValue);
                        
                    } catch (NoSuchFieldException e) {
                        System.err.println("[BehaviourSpec]: Warning - Field '" + fieldName + 
                                         "' not found in script " + script);
                    }
                }
            }

            // Store refs for later resolution (after scene is fully loaded)
            if (refs != null && !refs.isEmpty()) {
                behaviour.setPendingReferences(refs);
            }
            
            System.out.println("[BehaviourSpec]: Created " + script + " behaviour");
            return behaviour;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create behaviour script: " + script, e);
        }
    }
    
    /**
     * Convert YAML values to appropriate Java types
     */
    private Object convertValue(Object value, Field field) {
        if (value == null) return null;

        Class<?> targetType = field.getType();
        Type genericType = field.getGenericType();

        // --- Handle List<T> ---
        if (List.class.isAssignableFrom(targetType)) {

            if (!(value instanceof List<?> yamlList)) {
                throw new IllegalArgumentException(
                    "Expected a list for field '" + field.getName() + "'"
                );
            }

            // Determine T (generic type parameter)
            if (genericType instanceof ParameterizedType pType) {
                Type elementType = pType.getActualTypeArguments()[0];

                if (elementType instanceof Class<?> elementClass) {
                    List<Object> result = new ArrayList<>();

                    for (Object element : yamlList) {
                        result.add(convertSingleValue(element, elementClass));
                    }

                    return result;
                }
            }

            // No generic info -> return raw list
            return value;
        }

        // --- Single value conversion (old rules) ---
        return convertSingleValue(value, targetType);
    }

    private Object convertSingleValue(Object value, Class<?> targetType){
        if (value == null) return null;
        
        // Already correct type
        if (targetType.isInstance(value)) {
            return value;
        }

        // Vec3 conversion from list [x, y, z]
        if (targetType == Vec3.class) {
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (list.size() == 3) {
                    float x = toFloat(list.get(0));
                    float y = toFloat(list.get(1));
                    float z = toFloat(list.get(2));
                    return new Vec3(x, y, z);
                } else {
                    throw new IllegalArgumentException("Vec3 requires exactly 3 values, got " + list.size());
                }
            }
        }
        
        // Vec2 conversion from list [x, y]
        if (targetType == Vec2.class) {
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (list.size() == 2) {
                    float x = toFloat(list.get(0));
                    float y = toFloat(list.get(1));
                    return new Vec2(x, y);
                } else {
                    throw new IllegalArgumentException("Vec2 requires exactly 2 values, got " + list.size());
                }
            }
        }
        
        // Number conversions
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == float.class || targetType == Float.class) {
                return num.floatValue();
            } else if (targetType == int.class || targetType == Integer.class) {
                return num.intValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return num.doubleValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return num.longValue();
            }
        }
        
        // String conversion
        if (targetType == String.class) {
            return value.toString();
        }
        
        // Boolean conversion
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) return value;
            return Boolean.parseBoolean(value.toString());
        }
        
        // Default: return as-is and inshallah it works
        return value;
    }

    // Helper method to convert any object to float
    private float toFloat(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return Float.parseFloat(o.toString());
    }
}