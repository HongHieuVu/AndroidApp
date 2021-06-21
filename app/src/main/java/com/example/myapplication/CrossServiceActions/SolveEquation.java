package com.example.myapplication.CrossServiceActions;

import com.example.myapplication.Calculator;
import com.example.myapplication.Exceptions.IllegalOperator;
import com.example.myapplication.Exceptions.NoSolution;
import com.example.myapplication.Exceptions.NotAnEquation;

import java.util.Locale;

public class SolveEquation extends Actions{
    public SolveEquation(String equation){
        setResult(() -> {
            Calculator calculator = Calculator.getCalculator();
            try {
                return String.format(Locale.US,"%s", calculator.solveAll(equation));
            } catch (IllegalOperator | NoSolution | NotAnEquation calculatorError) {
                calculatorError.printStackTrace();
                return calculatorError.getMessage();
            }
        });
    }
}
