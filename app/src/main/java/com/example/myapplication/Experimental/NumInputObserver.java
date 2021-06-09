package com.example.myapplication.Experimental;

public interface NumInputObserver extends Observer {
    StringBuilder userInput = new StringBuilder();

    @Override
    default void update(Subject changedSubject) {
        userInput.setLength(0);
        userInput.append(((NumInputSubject) changedSubject).getState());
    }

    default String getInput(){
        return userInput.toString();
    }
}
