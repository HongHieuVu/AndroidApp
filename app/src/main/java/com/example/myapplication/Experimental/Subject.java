package com.example.myapplication.Experimental;

import java.util.ArrayList;
import java.util.List;

public interface Subject {
    List<Observer> observers = new ArrayList<>();

    default void attach(Observer observer){
        observers.add(observer);
    }

    default void detach(Observer observer){
        observers.remove(observer);
    }

    default void notifyObservers(Subject changedSubject){
        for (Observer observer: observers) {
            observer.update(changedSubject);
        }
    }
}
