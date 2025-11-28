package engine.data;

import java.util.List;
import java.util.Map;

public class GameObjectSpec {
    public String name;
    public List<ComponentSpec> components; // strong typed ComponentSpecs
    public List<GameObjectSpec> children; // child GameObjects
}

