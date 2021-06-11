package com.example.myapplication;

public class Autofill extends Services{
    private static Autofill autofill = null;

    private Autofill(){}

    public static Autofill getAutofill() {
        if (autofill == null) return new Autofill();
        return autofill;
    }
}
