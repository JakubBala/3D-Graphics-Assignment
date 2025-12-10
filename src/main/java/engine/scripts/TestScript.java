package engine.scripts;

import java.util.Vector;

import engine.components.Behaviour;
import engine.gmaths.Vec2;
import engine.gmaths.Vec3;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

public class TestScript extends Behaviour{

    public float someFloat;
    public int someInt;
    public String someString;
    public boolean someBoolean;
    public Vec3 someVec3;
    public Vec2 someVec2;
    public ReferencedScript referencedScript;

    public void Start() {
        System.out.println("TestScript Start called!");
        System.out.println("someFloat = " + someFloat);
        System.out.println("someInt = " + someInt);
        System.out.println("someString = " + someString);
        System.out.println("someBoolean = " + someBoolean);
        System.out.println("someVec3 = " + someVec3);
        System.out.println("someVec2 = " + someVec2);

        float referencedValue = referencedScript.getSomeValue();
        System.out.println("referencedScript.someValue = " + referencedValue);
    }

    @Override
    public void Update() {
        Vec3 targetPosition = new Vec3(5, 5, 5);
        getGameObject().getTransform().LookAt(targetPosition, new Vec3(0, 1, 0));
    }
}
