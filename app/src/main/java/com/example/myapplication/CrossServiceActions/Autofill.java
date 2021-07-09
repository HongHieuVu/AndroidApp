package com.example.myapplication.CrossServiceActions;

import com.example.myapplication.Calculator;

import java.util.LinkedList;
import java.util.List;

/**
 * get an autocomplete for operator input. This action has the memory of 2 latest
 * characters entered. Probability of two operators having the same two initial characters is zero.
 * Result is set to null if not autofill-able, else is set to what's left to be filled to complete
 * the operator.
 */
public class Autofill extends Actions{
    private static boolean canAutofill = false;
    private static char lastChar;
    List<Calculator.Operators> matchList = new LinkedList<>();

    /**
     * auto complete the equation, action result is null if no autofill can be done.
     * @param equation the current unfinished equation
     */
    public Autofill(String equation){
        setResult(() -> {
            if (equation.length() != 0){
                return equation + getFiller(equation.charAt(equation.length() - 1));
            }
            return equation;
        });
    }

    /**
     * get what's left to get an operator
     * @param newChar character user has just entered
     * @return what's left to be filled, null if nothing can be filled
     */
    private String getFiller(char newChar){
        if (Character.isDigit(newChar)) return ""; //no operator begins with a digit

        String operatorRep;
        for (Calculator.Operators op: Calculator.Operators.values()) {
            operatorRep = op.getOperatorStr();

            if (operatorRep.length() == 1) continue; //nothing to fill

            if (canAutofill){
                if (operatorRep.charAt(1) == newChar &&
                        operatorRep.charAt(0) == lastChar) {
                    matchList.add(op);
                }
                continue;
            }

            if (operatorRep.charAt(0) == newChar){
                canAutofill = true;
                lastChar = newChar;
                break;
            }
        }

        if (matchList.size() != 1) return "";
        canAutofill = false;
        return matchList.get(0).getOperatorStr().substring(2); //omits first 2 characters
    }
}
