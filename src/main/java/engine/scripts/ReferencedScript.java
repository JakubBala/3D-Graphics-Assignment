package engine.scripts;

import engine.components.Behaviour;

public class ReferencedScript extends Behaviour{
    private float someValue = 999f;

    public float getSomeValue() {
        return someValue;
    }
}
