package com.example.myapplication;

import com.example.myapplication.Exceptions.IllegalOperator;
import com.example.myapplication.Exceptions.NoSolution;
import com.example.myapplication.Exceptions.NotAnEquation;
import com.example.myapplication.Experimental.NumInputObserver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;
import java.util.Timer;

public class Calculator{
    //text parsing constants
    private final char CLOSE_PAR = ')'; //close par is the calculate order
    private final char SPACE = ' ';
    private final char END_ABS = '|';
    private final char DECIMAL_POINT_ENG = '.';
    private final char VARIABLE = 'x';
    private final char EQUALS = '=';

    public static final double TOLERANCE = Math.pow(10, -7);

    /**
     * enum of all operators
     */
    public enum Operators{
        //special operators (non-mathematical)
        OPEN_PAR("(", 0), //should have lowest order

        //mathematical operators
        ADD("+", 0,  () -> {
            double a = vals.pop(), b;
            if (vals.isEmpty()) b = 0;
            else b = vals.pop();
            return a + b;
        }),
        SUB("-", 1, () -> {
            double b = vals.pop(), a;
            if (vals.isEmpty()) a = 0;
            else a = vals.pop();
            return a - b;
        }),
        MUL("*", 2, () -> {
            double a = vals.pop();
            double b = vals.pop();
            return a * b;
        }),
        DIV("/", 2, () -> {
            double b = vals.pop();
            double a = vals.pop();
            return a / b;
        }),
        SQRT("sqrt", "Square root", 3, () -> {
            return Math.sqrt(vals.pop());}
            ),
        ABS("|", "Absolute value", 3, () -> {
            return Math.abs(vals.pop());}
            ),
        LOG("log", "log base a ( a log(b) )", 3, () -> {
            double b = vals.pop();
            double a = vals.pop();
            return Math.log(b) / Math.log(a);
        }),
        NLOG("ln", "ln b (natural logarithm)", 3, () -> {
            return Math.log(vals.pop());}
            ),
        EXP("^", "a to the b", 3, () -> {
            double b = vals.pop();
            double a = vals.pop();
            if (complexDeg) deg += b; //if this is exponent of a variable
            complexDeg = false;
            if (deg > maxDeg) maxDeg = deg;
            deg = 0;
            return Math.pow(a, b);
        }),
        SIN("sin", 3, () -> {
            return Math.sin(vals.pop());}
            ),
        COS("cos", 3, () -> {
            return Math.cos(vals.pop());}
            ),
        TAN("tan", 3, () -> {
            double a = vals.pop();
            return Math.sin(a) / Math.cos(a);}
            ),
        COT("cot", 3, () -> {
            double a = vals.pop();
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
            assert this.operation != null;
            return this.operation.execute();
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

    private static Calculator cal;
    private static Stack<Operators> ops;
    private static Stack<Double> vals;

    //starting value of x in solve() method. Should be close to 1 but within domain of all operators
    private static double startVal = 1;
    private static double var = 0.12; //what gets inserted in place of a variable
    private static double DEFAULT_START_VAL = 1;

    //get the degree of the equation
    private static double maxDeg = 0;
    private static double deg = 0;
    private static boolean complexDeg = false;

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
     * solve the equation. The part on the right hand side of an equation will be inverted.
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
        deg = 0;

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
                    if (buffer.charAt(0) == Operators.EXP.operatorStr.charAt(0)){
                        complexDeg = true;
                        continue;
                    }
                    deg ++;
                    if (deg > maxDeg) maxDeg = deg;
                    deg = 0;    //reset after use
                    continue;
                }

                //inverts right hand side
                if (c == EQUALS){
                    while (!ops.isEmpty()){
                        backCalculate();
                    }
                    ops.push(Operators.OPEN_PAR);
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
     * Solves for a new root of an equation
     * @param input equation to solve
     * @return the root, null if no more root can be found
     * @throws NoSolution if no solution found after an amount of time (10 sec)
     * @throws IllegalOperator if an undefined operator is found, or operators doesn't make mathematical
     * sense
     */
    private Double solve(String input, List<Root> knownRoots) throws NoSolution, IllegalOperator {
        double tolerance = TOLERANCE;  //solution tolerance accepted
        long timeAllowed = 2;     //time tolerated to solve (sec)
        int trialsAllowed = 3;      //times hitting a known root before aborting
        double dx = 0.001;          //step size
        var = startVal;            //set initial variable value

        long startTime = System.currentTimeMillis(), currTime, elapsed;

        int trial = 1;
        double y;
        double derivative;
        do {
            //get value of y(x) at current x
            y = calculate(input);

            //calculate derivative at current x
            var += dx;
            double dy = calculate(input) - y; //result of calculate has changed since var changed
            derivative = dy / dx;

            //update x
            var = var - (y / derivative);

            //verify new x
            if (var == Double.NEGATIVE_INFINITY || var == Double.POSITIVE_INFINITY){
                startVal = - startVal * 1.2;
                var = startVal;
            }

            //trial
            if (knownRoots.contains(new Root(var))){
                trial++;
                if (trial > trialsAllowed) return null;
                startVal = - startVal * 1.2;
                var = startVal;
            }

            //update step size
            dx = dx / 2;

            //checks if time limit exceeded
            currTime = System.currentTimeMillis();
            elapsed = (currTime - startTime) / 1000; //milisec to sec

            //TODO: delete this debugger
            System.out.println(" var: " + var +
                    " continue? " + (Math.abs(y) > tolerance) +
                    " time elapsed: " + elapsed +
                    " known? " + knownRoots.contains(new Root(var)) +
                    " trial: " + trial);
        } while (Math.abs(y) > tolerance && elapsed < timeAllowed);

        if (elapsed > timeAllowed)
            throw new NoSolution("No solution found in reasonable time");

        startVal = DEFAULT_START_VAL;
        return var;
    }

    /**
     * get all roots possible from an equation, to the best that this tuned calculator can find.
     * NO GUARANTEE THAT THIS METHOD CAN FIND ALL ROOTS due to the mathematical limitations of
     * the Newton-Raphson method, this is best effort.
     * @param input equation to solve
     * @return string representation of all roots found
     * @throws NotAnEquation when input is not an equation
     * @throws NoSolution when no solution found within reasonable time
     * @throws IllegalOperator an illegal operator is found
     */
    public String solveAll(String input) throws NotAnEquation, NoSolution, IllegalOperator {
        if (input.indexOf(VARIABLE) == -1) throw new NotAnEquation("This is not an equation with variable");

        List<Root> roots = new ArrayList<>();

        //loop through solve() to find those roots
        long startTime = System.currentTimeMillis(), currTime, elapsed;
        double timeAllowed = 5;
        Double newRoot;
        do {

            //find new root
            newRoot = solve(input, roots);
            if (newRoot == null) break;
            roots.add(new Root(newRoot));
            System.out.println("Added: " + newRoot);

            //change startVal to avoid stepping into known val
            startVal = -1.2 * startVal;

            //check time
            currTime = System.currentTimeMillis();
            elapsed = (currTime - startTime) / 1000; //milisec to sec

        } while (roots.size() < maxDeg && elapsed < timeAllowed);

        if (roots.isEmpty()) throw new NoSolution("Found no solution");

        //TODO: delete this debugger
        System.out.println(roots.toString());
        System.out.println(maxDeg);

        //constructs response:
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < roots.size(); i++){
            response.append(String.format(Locale.US,"root %o: %.5f\n", i, roots.get(i).getValue()));
        }

        return response.toString();
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
