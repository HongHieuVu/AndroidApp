package com.example.myapplication;

import android.graphics.Path;

import com.example.myapplication.Exceptions.IllegalOperator;

import java.util.Scanner;
import java.util.Stack;

public class Calculator {
    /**
     * enum of all operators
     */
    public enum Operators{
        STOP("(", 4),
        ADD("+", 1,  (double a, double b) -> a + b),
        SUB("-", 1, (double a, double b) -> a - b),
        MUL("*", 2, (double a, double b) -> a * b),
        DIV("/", 2, (double a, double b) -> a / b),
        SQRT("sqrt", "Square root", 3,
                (double ignored, double a) -> {
            vals.push(ignored);
            return Math.sqrt(a);}
            ),
        ABS("|", "Absolute value", 3, (double ignored, double a) -> {
            vals.push(ignored);
            return Math.abs(a);}
            ),
        LOG("log", "log base a ( a log(b) )", 3,
                (double a, double b) -> Math.log(b) / Math.log(a)
            ),
        NLOG("ln", "ln b (natural logarithm)", 3,
                (double ignored, double a) -> {
            vals.push(ignored);
            return Math.log(a);}
            ),
        EXP("^", "a to the b", 3,
                (double a, double b) -> Math.pow(a, b)
            ),
        SIN("sin", 3, (double ignored, double a) -> {
            vals.push(ignored);
            return Math.sin(a);}
            ),
        COS("cos", 3, (double ignored, double a) -> {
            vals.push(ignored);
            return Math.cos(a);}
            ),
        TAN("tan", 3, (double ignored, double a) -> {
            vals.push(ignored);
            return Math.sin(a) / Math.cos(a);}
            ),
        COT("cot", 3, (double ignored, double a) -> {
            vals.push(ignored);
            return Math.cos(a) / Math.sin(a);}
            );

        /**
         * intended to mark operators with conventional names different from what user type in. Will
         * be called to build user guide.
         */
        private final String normalName;

        /**
         * what user types in to call this operator
         */
        private final String operatorStr;

        /**
         * the function of this operator
         */
        private final Operation operation;

        /**
         * higher order of execution will be executed first.
         */
        private final int orderOfExec;

        Operators(String operatorStr, int orderOfExec){
            this.operatorStr = operatorStr;
            this.normalName = null;
            this.operation = null;
            this.orderOfExec = orderOfExec;
        }

        Operators(String operatorStr, int orderOfExec, Operation operation) {
            this.normalName = null;
            this.orderOfExec = orderOfExec;
            this.operatorStr = operatorStr;
            this.operation = operation;
        }

        /**
         * intended for operators with special representations in the calculator
         * @param operatorStr what user types in to call this operator
         * @param orderOfExec precedence, higher means first
         * @param normalName example use
         * @param operation function of this operator
         */
        Operators(String operatorStr, String normalName, int orderOfExec, Operation operation) {
            this.operatorStr = operatorStr;
            this.normalName = normalName;
            this.orderOfExec = orderOfExec;
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

    private final char CLOSE_PAR = ')'; //close par is the calculate order
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

    /**
     * solve the equation
     * @param input input equation
     * @return the result
     * @throws IllegalOperator when there's an operator unfamiliar to the calculator in the equation
     */
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
                if (c == SPACE) continue;

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
                    if (ops.isEmpty()) continue;
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

        //push this building number
        if (number != null){
            Scanner numScnr = new Scanner(number.toString());
            vals.push(numScnr.nextDouble());
        }

        //execute all recorded operations
        while (!ops.empty()){
            Operators op = ops.pop();
            vals.push(op.calculate());
        }

        return vals.pop();
    }

    /**
     * Lists all special operators known to the calculator.
     * Special operators are ones with a normal name different from what we type in
     * @return a string representing all special operators and their symbols
     */
    public String listAllSpecialOps(){
        StringBuilder opList = new StringBuilder();
        for (Operators op: Operators.values()) {
            if (op.normalName != null) {
                String line = op.normalName + ": " + op.operatorStr + "\n";
                opList.append(line);
            }
        }
        return opList.toString();
    }
}
