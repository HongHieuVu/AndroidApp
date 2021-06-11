package com.example.myapplication;

import com.example.myapplication.Exceptions.IllegalOperator;
import com.example.myapplication.Exceptions.NoSolution;
import com.example.myapplication.Experimental.NumInputObserver;

import java.util.EmptyStackException;
import java.util.Scanner;
import java.util.Stack;
import java.util.Timer;

public class Calculator{

    /**
     * enum of all operators
     */
    public enum Operators{
        OPEN_PAR("(", 0), //should have lowest order
        ADD("+", 0,  (double a, double b) -> a + b),
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
            double second = vals.pop(), first;
            if (vals.isEmpty()) first = 0.0; //infinite zeros
            else first = vals.pop(); //op must push back sec if it only requires first
            assert this.operation != null;
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
    private final char SPACE = ' ';
    private final char END_ABS = '|';
    private final char DECIMAL_POINT_ENG = '.';
    private final char VARIABLE = 'x';
    private final char EQUALS = '=';

    private static Calculator cal;
    private static Stack<Operators> ops;
    private static Stack<Double> vals;
    private static double var = 0.12;

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
     * solve the equation. Calling this method on a double sided equation returns the left hand side
     * minus the right hand side
     * @param input input equation
     * @return the result
     * @throws IllegalOperator when there's an operator unfamiliar to the calculator in the equation
     * @see Calculator#backCalculate()
     */
    public Double calculate(String input) throws IllegalOperator, EmptyStackException {
        vals.clear();
        vals.push(0.0); //so that one arg ops have something to pop if they come first
        ops.clear();
        ops.push(Operators.OPEN_PAR); //to simplify backCalc method

        String buffer = input;
        StringBuilder longOp = null; //long operators are operators with more than one char
        StringBuilder number = null;

        while (!buffer.isEmpty()){

            //extracts a character
            char c = buffer.charAt(0);
            buffer = buffer.substring(1);

            if (Character.isDigit(c)) {

                //see if the previous operator is complete
                if (longOp != null){
                    Operators op = Operators.assignEnum(longOp.toString());
                    if (op == null)
                        throw new IllegalOperator("Illegal operator: " + longOp.toString());
                    if (ops.peek().orderOfExec > op.orderOfExec
                            && (op != Operators.OPEN_PAR)){
                        Operators prevOp = ops.pop();
                        vals.push(prevOp.calculate());
                    }
                    ops.push(op);
                    longOp = null;
                }

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
                    backCalculate();
                    continue;
                }

                //inserts variable
                if (c == VARIABLE){
                    vals.push(var);
                    continue;
                }

                //inverts right hand side
                if (c == EQUALS){
                    ops.push(Operators.SUB);
                    ops.push(Operators.OPEN_PAR);
                    continue;
                }

                //note the operator
                Operators op = Operators.assignEnum(Character.toString(c));
                if (op != null) {
                    if (ops.peek().orderOfExec > op.orderOfExec
                            && (op != Operators.OPEN_PAR)){
                        Operators prevOp = ops.pop();
                        vals.push(prevOp.calculate());
                    }
                    longOp = null;
                    ops.push(op);
                    continue;
                }

                if (longOp == null) longOp = new StringBuilder();
                longOp.append(c);
            }
        }

        //push this remaining number
        if (number != null){
            Scanner numScnr = new Scanner(number.toString());
            vals.push(numScnr.nextDouble());
        }

        //execute all recorded operations
        while (!ops.isEmpty()){
            backCalculate();
        }
        return vals.pop();
    }

    /**
     * calculates backward by popping stack until nothing left.
     * Stops if encountered an open parenthesis or stack is empty.
     * While calculating backward, checks if the next operator in stack has higher order. If so that
     * operator will be executed first to flatten out order.
     */
    private void backCalculate(){
        while (ops.peek() != Operators.OPEN_PAR){
            Operators op = ops.pop();
            if (ops.peek().orderOfExec > op.orderOfExec){
                if (ops.peek() == Operators.SUB){
                    double tmp = vals.pop();
                    Operators prevOp = ops.pop();
                    vals.push(prevOp.calculate());
                    vals.push(tmp);
                } else {
                    Operators prevOp = ops.pop();
                    vals.push(prevOp.calculate());
                }
            }
            vals.push(op.calculate());
        }
        ops.pop(); //removes open par
    }

    /**
     *
     * @param input
     * @return
     * @throws NoSolution
     * @throws IllegalOperator
     */
    public Double solve(String input) throws NoSolution, IllegalOperator {
        double tolerance = Math.pow(0.1, 7);  //solution tolerance accepted
        long timeAllowed = 10;     //time tolerated to solve (sec)
        double startVal = 1;     //current x
        double dx = Math.pow(0.1, 2);       //step size
        var = startVal;            //set initial variable value

        long startTime = System.currentTimeMillis(), currTime, elapsed;

        double y;
        double derivative;
        do {
            //get value of y(x) at current x
            y = calculate(input);

            //calculate derivative at current x
            var += dx;
            double dy = calculate(input) - y; //result of calculate has changed since var changed
            derivative = dy / dx;

            //get new x
            var = var - (y / derivative);

            //verify new x
            if (var == Double.NEGATIVE_INFINITY || var == Double.POSITIVE_INFINITY){
                var = - startVal * 1.2;
            }

            //update step size
            dx = dx / 2;

            //checks if time limit exceeded
            currTime = System.currentTimeMillis();
            elapsed = (currTime - startTime) / 1000; //milisec to sec
            System.out.println(y + " " + (Math.abs(y) > tolerance));
        } while (Math.abs(y) > tolerance && elapsed < timeAllowed);

        if (elapsed > timeAllowed)
            throw new NoSolution("No solution found in reasonable time");

        return var;
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
