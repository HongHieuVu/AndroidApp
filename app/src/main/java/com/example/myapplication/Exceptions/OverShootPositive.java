package com.example.myapplication.Exceptions;

/**
 * once this occurs, the limit of this method is reached
 */
public class OverShootPositive extends Exception {
    public OverShootPositive(String message){
        super(message);
    }
}
