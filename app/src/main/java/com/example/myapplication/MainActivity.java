package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.CrossServiceActions.Calculate;
import com.example.myapplication.CrossServiceActions.Autofill;
import com.example.myapplication.CrossServiceActions.GetCalculatorInstruction;
import com.example.myapplication.CrossServiceActions.SolveEquation;

public class MainActivity extends AppCompatActivity {
    enum Modes{
        CALCULATING, EQ_SOLVING;

        public boolean isSolvingMode(){
            return this == Modes.EQ_SOLVING;
        }

        public boolean isCalculateMode(){
            return this == Modes.CALCULATING;
        }
    }

    TextView userGuide;
    EditText numInput;
    Button calculate;
    TextView result;
    Modes calculatorMode = Modes.CALCULATING;

    //TODO: add system of equations function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userGuide = findViewById(R.id.userGuide);
        numInput = findViewById(R.id.numInput);
        calculate = findViewById(R.id.calculate);
        result = findViewById(R.id.result);

        userGuide.setText(new GetCalculatorInstruction().getResult());
        result.setText(R.string.resultPrompt);

        numInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = numInput.getText().toString();

                //checks whether input is now an equation
                if (input.indexOf('=') != -1) calculatorMode = Modes.EQ_SOLVING;
                else calculatorMode = Modes.CALCULATING;
                calculate.setText(
                        calculatorMode == Modes.EQ_SOLVING ? R.string.Solve : R.string.calculate
                );

                //autofill operator for user
                if (input.length() != 0){
                    char newChar = input.charAt(input.length() - 1);
                    String filler = new Autofill(newChar).getResult();
                    if (filler != null) {
                        String newText = input + filler;
                        numInput.setText(newText);
                        numInput.setSelection(newText.length()); //set cursor
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        calculate.setOnClickListener(v -> {
            String input = numInput.getText().toString();
            if (calculatorMode.isCalculateMode())
                result.setText(new Calculate(input).getResult());
            else
                result.setText(new SolveEquation(input).getResult());
        });
    }
}