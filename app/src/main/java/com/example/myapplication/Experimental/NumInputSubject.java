package com.example.myapplication.Experimental;

public interface NumInputSubject  extends Subject {
    StringBuilder state = new StringBuilder();

    default void attachInputListener(NumInputObserver numInputObserver){
        attach(numInputObserver);
    }

    default String getState(){
        return state.toString();
    }

    default void setState(String newState){
        state.setLength(0);
        state.append(newState);
    }
}
