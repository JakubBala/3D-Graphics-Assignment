package engine.scripts;

import engine.components.Behaviour;
import engine.gmaths.Vec2;
import engine.gmaths.Vec3;
import engine.rendering.Material;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

public class CloudController extends Behaviour{



    @Override
    public void Start() {
        
    }

    @Override
    public void Update() {

        // Change Daylight cycle time uniform in cloud material
        float time = GameController.getDayLightCycle();
        Material cloudMaterial = getGameObject().getComponent(engine.components.MeshRenderer.class).getMaterial();
        cloudMaterial.setUniform("timeOfDay", time);

        // Move clouds slowly
        float speed = -0.03f;
        float offsetX = (float)(speed * GameController.getElapsedTime());
        cloudMaterial.setUniform("uvOffset", new Vec2(offsetX, 0.0f ));
    }
}
