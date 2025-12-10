package engine.data;

import java.util.List;
import java.util.Map;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class GameObjectSpec {
    public String name;
    public String id;
    public List<ComponentSpec> components; // strong typed ComponentSpecs
    public List<GameObjectSpec> children; // child GameObjects
}

