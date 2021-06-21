package com.example.myapplication;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * purpose of this class is to get a custom equals() method
 */
public class Root {
    private final double TOLERANCE = Math.pow(10, -6); //diff from calculator's tolerance
    private double value;

    public Root(double value){
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Root root = (Root) o;
        return Math.abs(this.value - root.getValue()) < TOLERANCE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @NotNull
    @Override
    public String toString() {
        return "" + value;
    }
}
