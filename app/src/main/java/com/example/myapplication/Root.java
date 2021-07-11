package com.example.myapplication;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

/**
 * purpose of this class is to get a custom equals() method
 */
public class Root {
    private final double value;

    public Root(double value){
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    /**
     * compare two roots
     * @param o other root
     * @return true if two roots are the same if rounded to 5 decimal places
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Root root = (Root) o;
        String thisNum = String.format(Locale.US,"%.5f", value);
        String thatNum = String.format(Locale.US,"%.5f", root.getValue());
        return thisNum.equals(thatNum);
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
