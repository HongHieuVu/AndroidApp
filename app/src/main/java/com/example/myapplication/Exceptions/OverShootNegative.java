package com.example.myapplication.Exceptions;

/**
 * once this occurs, the limit of this method is reached
 */
public class OverShootNegative extends Exception {
    public OverShootNegative(String message){
        super(message);
    }
}
