package com.example.myapplication.CrossServiceActions;

/**
 * The message services and controller send each other.
 * Exception handling and result formatting are delegated to this hierarchy tree in the subclasses.
 */
public abstract class Actions {
    String result;

    protected void setResult(MessageEffect messageEffect){
       result = messageEffect.send();
    }

    public String getResult(){
        return result;
    }
}
