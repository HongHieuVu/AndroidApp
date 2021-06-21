package com.example.myapplication.CrossServiceActions;

import com.example.myapplication.Calculator;

public class GetCalculatorInstruction extends Actions{
    public GetCalculatorInstruction(){
        setResult(() -> {
            Calculator calculator = Calculator.getCalculator();
            return calculator.listAllSpecialOps(); //for now, just this is enough for instructions
        });
    }
}
