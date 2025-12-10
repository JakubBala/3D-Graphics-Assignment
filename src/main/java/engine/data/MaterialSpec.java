package engine.data;

import java.util.Map;
import java.util.List;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/
public class MaterialSpec {
  public String vertex;        // vertex shader path
  public String fragment;      // fragment shader path
  public Map<String, Object> uniforms; // uniform name -> scalar or list
  public Map<String, String> textures; // logical name -> filepath
  public String name;
  public String id;
  public Boolean doubleSided;
  public Boolean transparent;
}