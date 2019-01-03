package bupt.gravity;

import java.util.ArrayList;
import java.util.Collections;

public class Calculation {

    private int state = 0; // 0:hold, 1:throwing, 2:done;
    private int throwingInterval = 25;//magic
    private double timeUnit = 0.02;
    private int fallingCount = 0;
    private int fallingMomentIndex = 0;
    private ArrayList<Double> sequence = new ArrayList<>();

    public double onDataArrive(double x, double y, double z){
        double magnitude = Math.sqrt(Math.pow(x, 2) + Math.pow(y ,2) + Math.pow(z, 2));

        stateTransition(magnitude);

        if (state < 2) {
            sequence.add(magnitude);
            return 0;
        }
        else {
            return calcDistance();
        }
    }


    private void stateTransition(double magnitude){
        if (state == 0){
            if (magnitude > 9 && magnitude < 11)
                fallingCount++;
            else fallingCount = 0;
            if (fallingCount > 8){
                state = 1;
                fallingMomentIndex = sequence.size() - 9;//magic
            }
        }
        else if (state == 1){
            if (magnitude > 25){//magic
                state = 2;
            }
            fallingCount++;
        }
        // else 2
    }

    private double findAccPeak(){
        return Collections.max(sequence.subList(fallingMomentIndex-throwingInterval, fallingMomentIndex));
    }


    private double calcDistance(){
        double accPeak = findAccPeak();
        double speed = accPeak * throwingInterval * timeUnit * 0.5;
        return speed * fallingCount * timeUnit;
    }

}