package com.example.myapplication.CrossServiceActions;

import com.example.myapplication.Calculator;
import com.example.myapplication.Exceptions.IllegalOperator;

import java.util.EmptyStackException;
import java.util.Locale;

public class Calculate extends Actions {
    public Calculate(String input){
        setResult(() -> {
            Calculator calculator = Calculator.getCalculator();
            if (calculator == null) return "";
            try {
                return String.format(Locale.US,"%.5f", calculator.calculate(input));
            } catch (IllegalOperator illegalOperator) {
                illegalOperator.printStackTrace();
                return illegalOperator.getMessage();
            } catch (EmptyStackException emptyStackException){
                return "Wrong Arithmetic";
            }
        });
    }
}
