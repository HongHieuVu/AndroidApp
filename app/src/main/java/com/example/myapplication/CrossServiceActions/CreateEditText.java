package com.example.myapplication.CrossServiceActions;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.myapplication.MainActivity;
import com.example.myapplication.Modes;
import com.example.myapplication.R;

import java.util.LinkedList;
import java.util.List;

public class CreateEditText extends Actions{

    public static class EquationStack{
        View parent;
        LinearLayout linearLayout;
        EditText equation;
        Button removeEquation;

        public EquationStack(LinearLayout parent, MainActivity mainActivity){
            this.parent = parent;

            //create edit text field
            equation = new EditText(parent.getContext());
            LinearLayout.LayoutParams equationLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (float) 0.9
            );
            equation.setLayoutParams(equationLayoutParams);
            equation.setGravity(Gravity.CENTER);
            equation.setInputType(InputType.TYPE_CLASS_TEXT);
            equation.setTextSize(30); //unit: sp
            equation.setHint("Enter equation");

            //create remove equation button
            removeEquation = new Button(parent.getContext());
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (float) 0.1
            );
            removeEquation.setLayoutParams(buttonLayoutParams);
            removeEquation.setText(R.string.LessEquation);

            //add the two to a horizontal linear layout
            linearLayout = new LinearLayout(parent.getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.addView(equation);
            linearLayout.addView(removeEquation);

            //add view to parent
            parent.addView(linearLayout, 2);

            //set button effect
            removeEquation.setOnClickListener(v -> {
                equationStackList.remove(this);
                parent.removeView(this.linearLayout);
            });

            //set edit text effect (autofill and auto mode change)
            equation.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String input = equation.getText().toString();

                    //checks whether input is now an equation to change mode
                    if (input.indexOf('=') != -1) Modes.calculatorMode = Modes.EQ_SOLVING;
                    else Modes.calculatorMode = Modes.CALCULATING;
                    mainActivity.calculate.setText(Modes.calculatorMode.getButtonMsg());


                    //autofill operator for user, must be on this thread
                    String newText = new Autofill(input).getResult();
                    if (!newText.equals(input)) {
                        equation.setText(newText);
                        equation.setSelection(newText.length()); //set cursor
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    public static List<EquationStack> equationStackList = new LinkedList<>();

    public CreateEditText(LinearLayout parent, MainActivity mainActivity){
        setResult(() -> {
            EquationStack equationStack = new EquationStack(parent, mainActivity);
            equationStackList.add(equationStack);
            return "";
        });
    }
}
