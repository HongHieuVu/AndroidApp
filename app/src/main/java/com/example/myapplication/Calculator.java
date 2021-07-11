package com.example.myapplication;

import com.example.myapplication.Exceptions.IllegalOperator;
import com.example.myapplication.Exceptions.NoSolution;
import com.example.myapplication.Exceptions.NotAnEquation;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;

/**
 * provides calculation and equation solving services. Class written with multithreading in mind.
 */
public class Calculator {
    //text parsing constants
    private final char CLOSE_PAR = ')'; //close par is the calculate order
    private final char SPACE = ' ';
    private final char END_ABS = '|';
    private final char DECIMAL_POINT_ENG = '.';
    private final char VARIABLE = 'x';
    private final char EQUALS = '=';

    private static boolean endAbs = false;

    /**
     * enum of all operators
     */
    public enum Operators {
        //special operators (non-mathematical)
        START(null, -1), //denotes start of calculation
        PAR("(", -1), //should have lowest order
        ABS("|", "Absolute value", -1, () -> null
        ),

        //mathematical operators
        ADD("+", -1, () -> {
            double a = vals.pop(), b;
            if (vals.isEmpty() || ops.peek() == PAR || ops.peek() == ABS)
                b = 0;
            else b = vals.pop();
            return a + b;
        }),
//        SUB("-", 0, () -> {
//            double b = vals.pop(), a;
//            if (vals.isEmpty() || ops.peek() == ABS) a = 0; //behaves like INVERT at the beginning of expressions
//            else a = vals.pop();
//            return a - b;
//        }),
        SUB("-", 0, () -> {
//            if (ops.peek() != OPEN_PAR && ops.peek() != ABS)
                ops.push(ADD);
            return -1 * vals.pop();
}),
//        INVERT(null, 1, () -> -1 * vals.pop()),
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
            return Math.sqrt(vals.pop());
        }
        ),
        LOG("log", "log base a ( a log(b) )", 3, () -> {
            double b = vals.pop();
            double a = vals.pop();
            return Math.log(b) / Math.log(a);
        }),
        NLOG("ln", "ln b (natural logarithm)", 3, () -> {
            return Math.log(vals.pop());
        }
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
            return Math.sin(vals.pop());
        }
        ),
        COS("cos", 3, () -> {
            return Math.cos(vals.pop());
        }
        ),
        TAN("tan", 3, () -> {
            double a = vals.pop();
            return Math.sin(a) / Math.cos(a);
        }
        ),
        COT("cot", 3, () -> {
            double a = vals.pop();
            return Math.cos(a) / Math.sin(a);
        }
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

        Operators(String operatorStr, int orderOfExec) {
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
         *
         * @param operatorStr what user types in to call this operator
         * @param orderOfExec precedence, higher means first
         * @param normalName  example use
         * @param operation   function of this operator
         */
        Operators(String operatorStr, String normalName, int orderOfExec, Operation operation) {
            this.operatorStr = operatorStr;
            this.normalName = normalName;
            this.orderOfExec = orderOfExec;
            this.operation = operation;
        }

        /**
         * executes the calculation. Operators will pop the necessary amount of arguments from stack
         *
         * @return result of calculation
         */
        public Double calculate() {
            assert this.operation != null;
            return this.operation.execute();
        }

        public String getOperatorStr() {
            return this.operatorStr;
        }

        /**
         * converts from String to corresponding enum
         *
         * @param operator string for conversion
         * @return corresponding operator
         */
        public static Operators assignEnum(String operator) {
            for (Operators op : Operators.values()) {
                if (operator.equals(op.getOperatorStr())) return op;
            }
            return null; //what to do if illegal char or unidentified operator (basically the same)?
        }
    }

    //operator and values stack
    private static Calculator cal;
    private static Stack<Operators> ops;
    private static Stack<Double> vals;

    //multithreading trick
    private static boolean busy = false; //set to true when any calculation is happening

    //starting value of x in solve() method. Should be close to 1 but within domain of all operators
    private static final double DEFAULT_START_VAL = 1;
    private static double x = 1; //what gets inserted in place of a variable

    //get the degree of the equation
    private static double maxDeg = 0;
    private static double deg = 0;
    private static boolean complexDeg = false;

    private Calculator() {
        ops = new Stack<>();
        vals = new Stack<>();
    }

    /**
     * get a calculator instance
     *
     * @return a new calculator if no calculator has ever been created,
     * else return the previously created calculator instance (Singleton).
     * If multiple threads are trying to create a calculator, only the first will get and all
     * calculator request while it's busy will be met with null.
     */
    public static Calculator getCalculator() {
        if (cal != null) {
            if (busy) return null;
            return cal;
        }
        cal = new Calculator();
        return cal;
    }

    /**
     * solve the equation. The part on the right hand side of an equation will be inverted.
     *
     * @param input input equation
     * @return the result
     * @throws IllegalOperator when there's an operator unfamiliar to the calculator in the equation
     * @see Calculator#backCalculate()
     */
    public Double calculate(String input) throws IllegalOperator, EmptyStackException {
        busy = true;
        vals.clear();
        vals.push(0.0); //so that one arg ops have something to pop if they come first
        ops.clear();
        ops.push(Operators.PAR); //to simplify backCalc method
        ops.push(Operators.ADD);
        deg = 0;

        String buffer = input;
        StringBuilder longOp = null; //long operators are operators with more than one char
        StringBuilder number = null;

        while (!buffer.isEmpty()) {
            //pre process
            buffer = buffer.replaceAll("\\(", "(+");

            //extracts a character
            char c = buffer.charAt(0);
            buffer = buffer.substring(1);

            if (Character.isDigit(c)) {

                //see if the previous operator is complete
                if (longOp != null) {
                    Operators op = Operators.assignEnum(longOp.toString());
                    if (op == null)
                        throw new IllegalOperator("Illegal operator: " + longOp.toString());
                    if (ops.peek().orderOfExec >= op.orderOfExec
                            && (op != Operators.PAR)) {
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
                if (c == DECIMAL_POINT_ENG) {
                    if (number == null) number = new StringBuilder();
                    number.append(c);
                    continue;
                }

                //inserts variable
                if (c == VARIABLE) {

                    //allows skipping the multiplication sign in front of a variable
                    if (number != null) {
                        Scanner numScnr = new Scanner(number.toString());
                        vals.push(numScnr.nextDouble());
                        number = null;
                        ops.push(Operators.MUL);
                    }

                    vals.push(x);

                    //note the equation degree
                    if (!buffer.isEmpty() && buffer.charAt(0) == '^') {
                        complexDeg = true;
                        continue;
                    }
                    deg++;
                    if (deg > maxDeg) maxDeg = deg;
                    deg = 0;    //reset after use
                    continue;
                }

                //builds the number right before the operator
                if (number != null) {
                    Scanner numScnr = new Scanner(number.toString());
                    vals.push(numScnr.nextDouble());
                    number = null;
                }

                //calculate
                if (c == CLOSE_PAR | c == END_ABS) {
                    if (c == END_ABS) {
                        if (endAbs) {
                            backCalculate();
                            endAbs = false;
                        } else {
                            endAbs = true;
                            ops.push(Operators.ABS);
                        }
                        continue;
                    }

                    backCalculate();
                    continue;
                }

                //inverts right hand side
                if (c == EQUALS) {
                    buffer = "-(" + buffer + ")";
                    continue;
                }

                //note the operator
                Operators op = Operators.assignEnum(Character.toString(c));
                if (op != null) {
                    if (ops.peek().orderOfExec > op.orderOfExec
                            && (op != Operators.PAR)
                            && (op != Operators.ABS)) {
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
        if (number != null) {
            Scanner numScnr = new Scanner(number.toString());
            vals.push(numScnr.nextDouble());
        }

        //execute all recorded operations
        while (!ops.isEmpty()) {
            backCalculate();
        }
        busy = false;
        return vals.pop();
    }

    /**
     * calculates backward by popping stack until nothing left.
     * Stops if encountered an open parenthesis or stack is empty.
     * While calculating backward, checks if the next operator in stack has higher order. If so that
     * operator will be executed first to flatten out order.
     */
    private void backCalculate() {
        while (ops.peek() != Operators.PAR && ops.peek() != Operators.ABS) {
            Operators op = ops.pop();
            if (!ops.isEmpty() && ops.peek().orderOfExec > op.orderOfExec) {
                double tmp = vals.pop();
                Operators prevOp = ops.pop();
                vals.push(prevOp.calculate());
                vals.push(tmp);
                ops.push(op);
                continue;
            }
            vals.push(op.calculate());
        }
        if (ops.peek() == Operators.ABS)
            vals.push(Math.abs(vals.pop())); //TODO: check out this check
        ops.pop(); //removes open par
    }

    /**
     * Solves for a new root of an equation
     *
     * @param input equation to solve
     * @return the root, null if no more root can be found
     * @throws NoSolution      if no solution found after an amount of time (10 sec)
     * @throws IllegalOperator if an undefined operator is found, or operators doesn't make mathematical
     *                         sense
     */
    private Double findRoot(String input, List<Root> knownRoots, double startVal)
            throws NoSolution, IllegalOperator, EmptyStackException {

        double tolerance = Math.pow(10, -7);;  //solution tolerance accepted
        long timeAllowed = 3;       //time tolerated to solve (sec)
        double dx = 0.00000001;     //step size
        x = startVal;               //set initial variable value
        boolean positive;           //has it crossed the y = 0 line?
        long startTime = System.currentTimeMillis(), currTime, elapsed = 0;
        double y = calculate(input);
        positive = y > 0;
        double derivative;

        while (Math.abs(y) > tolerance) {

            //TODO: delete this debugger
            String peek = String.format(Locale.US,
                    "y: %15.10e " +
                            "var: %15.10f " +
                            "continue? " + (Math.abs(y) > tolerance) + " " +
                            "time elapsed: %2d "
                    , y, x, elapsed);
            System.out.println(peek);

            //checks if time limit exceeded
            currTime = System.currentTimeMillis();
            elapsed = (currTime - startTime) / 1000; //milisec to sec
            if (elapsed > timeAllowed)
                throw new NoSolution("No solution found in reasonable time");

            //exit Newton method once crossed the y = 0 line
//            if (positive != y > 0){
//
//                //binary search method
//                //TODO: write this method
//                continue;
//            }

            //calculate derivative at current x
            x += dx;
            double dy = calculate(input) - y; //result of calculate has changed since var changed
            derivative = dy / dx;

            //update x
            x = x - (y / derivative);

            //verify new x
            if (x == Double.NEGATIVE_INFINITY || x == Double.POSITIVE_INFINITY) {
                startVal = - startVal * 1.2;
                x = startVal;
            }

            //update step size
            dx = dx / 6;
                //one peculiar note: the rate at which y approaches 0 after first few iterations
                // is equal to the step size reduction rate! (true for ~10^-3 init step size)
                //however, reducing initial step size is more effective

            //get value of y(x) at current x
            y = calculate(input);
        }

        return x;
    }

    /**
     * get all roots possible from an equation, to the best that this tuned calculator can find.
     * NO GUARANTEE THAT THIS METHOD CAN FIND ALL ROOTS due to the mathematical limitations of
     * the Newton-Raphson method, this is best effort.
     *
     * @param input equation to solve
     * @return string representation of all roots found
     * @throws NotAnEquation   when input is not an equation
     * @throws NoSolution      when no solution found within reasonable time
     * @throws IllegalOperator an illegal operator is found
     */
    public String solve(String input) throws NotAnEquation, NoSolution, IllegalOperator, EmptyStackException {
        busy = true;
        if (input.indexOf(VARIABLE) == -1)
            throw new NotAnEquation("This is not an equation with variable");

        double startVal = DEFAULT_START_VAL;
        int trial = 1, trialsAllowed = 3;       //times hitting a known root before aborting
        List<Root> roots = new ArrayList<>();   //stores known roots

        //loop through solve() to find those roots
        long startTime = System.currentTimeMillis(), currTime, elapsed = 0;
        double timeAllowed = 5;
        Double newRoot;
        do {

            //find new root
            newRoot = findRoot(input, roots, startVal);

            //trial limit and duplication check
            if (roots.contains(new Root(x))) {
                trial++;
                System.out.println(trial);
                if (trial > trialsAllowed){
                    if (roots.isEmpty())
                        throw new NoSolution("Trial limit exceeded");
                    break;
                }
                startVal = -startVal * 1.2; //in hope it will turn out a new root
                continue;
            }

            roots.add(new Root(newRoot));

            //TODO: delete this debugger
            System.out.println("Added: " + newRoot);

            //change startVal to avoid stepping into known val
            startVal = -1.2 * newRoot;

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
        for (int i = 0; i < roots.size(); i++) {
            response.append(String.format(Locale.US, "root %o: %.5f\n", i + 1, roots.get(i).getValue()));
        }

        busy = false;
        startVal = DEFAULT_START_VAL;
        return response.toString();
    }

    /**
     * Lists all special operators known to the calculator.
     * Special operators are ones with a normal name different from what we type in
     *
     * @return a string representing all special operators and their symbols
     */
    public String listAllSpecialOps() {
        StringBuilder opList = new StringBuilder();
        for (Operators op : Operators.values()) {
            if (op.normalName != null) {
                String line = op.normalName + ": " + op.operatorStr + "\n";
                opList.append(line);
            }
        }
        return opList.toString();
    }
}
