package com.example.myapplication;

import com.example.myapplication.Exceptions.IllegalOperator;

import java.util.Scanner;
import java.util.Stack;

public class Calculator {

    /**
     * enum of all operators
     */
    enum Operators{
        ADD("+", (double a, double b) -> a + b),
        SUB("-", (double a, double b) -> a - b),
        MUL("*", (double a, double b) -> a * b),
        DIV("/", (double a, double b) -> a / b),
        SQRT("sqrt", (double ignored, double a) -> {
            vals.push(ignored);
            return Math.sqrt(a);}
            ),
        ABS("|", (double ignored, double a) -> {
            vals.push(ignored);
            return Math.abs(a);}
            );

        private final String operatorStr;
        private final Operation operation;

        Operators(String operatorStr, Operation operation) {
            this.operatorStr = operatorStr;
            this.operation = operation;
        }

        /**
         * executes the calculation. Operators will pop the necessary amount of arguments from stack
         * @return result of calculation
         */
        public Double calculate() {
            double second = vals.pop(), first = vals.pop(); //op must push back sec if it only requires first
            return this.operation.execute(first, second);
        }

        public String getOperatorStr(){
            return this.operatorStr;
        }

        /**
         * converts from String to corresponding enum
         * @param operator string for conversion
         * @return corresponding operator
         */
        public static Operators assignEnum(String operator){
            for (Operators op: Operators.values()) {
                if (operator.equals(op.getOperatorStr())) return op;
            }
            return null; //what to do if illegal char or unidentified operator (basically the same)?
        }
    }

    private final char CLOSE_PAR = ')';
    private final char OPEN_PAR = '(';
    private final char SPACE = ' ';
    private final char END_ABS = '|';
    private final char DECIMAL_POINT_ENG = '.';

    private static Calculator cal;
    private static Stack<Operators> ops;
    private static Stack<Double> vals;

    private Calculator(){
        ops = new Stack<>();
        vals = new Stack<>();
    }

    public static Calculator getCalculator(){
        if (cal != null) return cal;
        cal = new Calculator();
        return cal;
    }

    public Double calculate(String input) throws IllegalOperator {
        String buffer = input;
        StringBuilder longOp = null; //long operators are operators with more than one char
        StringBuilder number = null;

        while (!buffer.isEmpty()){

            //extracts a character
            char c = buffer.charAt(0);
            buffer = buffer.substring(1);

            if (Character.isDigit(c)) {

                //see if the previous operator is complete
                if (longOp != null)
                    throw new IllegalOperator("Illegal operator: " + longOp.toString());

                //note the number
                if (number == null) number = new StringBuilder();
                number.append(c);
            } else {
                if (c == SPACE | c == OPEN_PAR) continue;

                //appends decimal point to the number
                if (c == DECIMAL_POINT_ENG){
                    if (number == null) number = new StringBuilder();
                    number.append(c);
                    continue;
                }

                //builds the number right before the operator
                if (number != null){
                    Scanner numScnr = new Scanner(number.toString());
                    vals.push(numScnr.nextDouble());
                    number = null;
                }

                //calculate
                if (c == CLOSE_PAR | c == END_ABS) {
                    Operators op = ops.pop();
                    vals.push(op.calculate());
                    continue;
                }

                //note the operator
                if (longOp == null) longOp = new StringBuilder();
                longOp.append(c);
                Operators op = Operators.assignEnum(longOp.toString());
                if (op == null) continue;
                ops.push(op);
                longOp = null;
            }
        }

        if (number != null){
            Scanner numScnr = new Scanner(number.toString());
            vals.push(numScnr.nextDouble());
        }

        Operators op = ops.pop();
        vals.push(op.calculate());

        return vals.pop();
    }
}
