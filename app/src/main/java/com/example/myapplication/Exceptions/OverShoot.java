package com.example.myapplication.Exceptions;

/**
 * once this occurs, the limit of this method is reached
 */
public class OverShoot extends Exception {
    private final boolean posX;

    public OverShoot(String message, boolean positiveX){
        super(message);
        posX = positiveX;
    }

    /**
     * get the sign of which the overshoot occurred
     * @return true if overshoot to the positive side of X
     */
    public boolean isPositive(){
        return posX;
    }
}
