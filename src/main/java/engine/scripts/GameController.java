package engine.scripts;

import engine.components.Behaviour;

public class GameController extends Behaviour {
    
    private static double startTime = 0f;
    private static float dayLightCycle = 0f;

    @Override
    public void Start() {
        startTime = getSeconds();
        updateDayLightCycle();
        System.out.println("GameController started!");
    }

    @Override
    public void Update() {
        updateDayLightCycle();
    }

    private void updateDayLightCycle() {
        double currentTime = getSeconds();
        dayLightCycle = (float)((currentTime - startTime) / 20.0 % 1.0); // 20 seconds for full cycle
        getGameObject().getScene().GetSkybox().SetTimeOfDay(dayLightCycle);
    }

    private static double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

    public static float getDayLightCycle() {
        return dayLightCycle;
    }

    public static double getElapsedTime() {
        return getSeconds() - startTime;
    }
}
