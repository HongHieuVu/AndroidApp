package com.example.myapplication;

public enum Modes{
    CALCULATING("Calculate"),
    EQ_SOLVING("Solve"),
    DIFFERENTIAL_EQ("Solve");

    String executeButtonMsg;
    public static Modes calculatorMode = Modes.CALCULATING;

    Modes(String buttonMessage){
        this.executeButtonMsg = buttonMessage;
    }

    public boolean isSolvingMode(){
        return this == Modes.EQ_SOLVING;
    }

    public boolean isCalculateMode(){
        return this == Modes.CALCULATING;
    }

    public boolean isDiffEqSolving(){
        return this == Modes.DIFFERENTIAL_EQ;
    }

    public String getButtonMsg(){
        return executeButtonMsg;
    }
}
