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
     * get what needed to complete an operator, action result is null if no autofill can be done.
     * @param newChar character the user just entered
     */
    public Autofill(char newChar){
        setResult(() -> {
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

            if (matchList.size() != 1) return null;
            canAutofill = false;
            return matchList.get(0).getOperatorStr().substring(2); //omits first 2 characters
        });
    }
}
