package engine.scripts;

import engine.components.Behaviour;
/* I declare that this code is my own work*/
/* Author Jakub Bala 
jbala1@sheffield.ac.uk
*/

public class GameController extends Behaviour {
    
    private static double startTime = 0f;
    // 0.0 = midnight, 0.25 = dawn, 0.5 = midday, 0.75 = dusk, 1.0 = midnight
    private static float dayLightCycle = 0f;

    private static double prevTime = 0f;
    private static double deltaTime = 0f;

    @Override
    public void Start() {
        startTime = getSeconds();
        updateDayLightCycle();
        System.out.println("GameController started!");
    }

    @Override
    public void Update() {
        double time = getElapsedTime();
        deltaTime = time - prevTime;
        prevTime = time;


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

    public static float getDeltaTime(){
        return (float)deltaTime;
    }
}
