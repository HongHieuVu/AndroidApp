package com.example.myapplication.Experimental;

import com.example.myapplication.Exceptions.IllegalOperator;

public interface CalculatorService {
    Double executeService(String input) throws IllegalOperator;
}
