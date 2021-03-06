package com.example.myapplication.CrossServiceActions;

import com.example.myapplication.Calculator;
import com.example.myapplication.Exceptions.IllegalOperator;
import com.example.myapplication.Exceptions.NoSolution;
import com.example.myapplication.Exceptions.NotAnEquation;

import java.util.EmptyStackException;
import java.util.Locale;

public class SolveEquation extends Actions {

    public SolveEquation(String equation){
        setResult(() -> {
            Calculator calculator = Calculator.getCalculator();
            if (calculator == null) return "Calculator working";
            try {
                return String.format(Locale.US,"%s", calculator.solve(equation));
            } catch (IllegalOperator | EmptyStackException | NotAnEquation | NoSolution calculatorError) {
                calculatorError.printStackTrace();
                return calculatorError.getMessage();
            }
        });
    }
}
