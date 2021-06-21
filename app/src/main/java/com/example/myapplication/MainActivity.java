package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Exceptions.IllegalOperator;
import com.example.myapplication.Exceptions.NoSolution;
import com.example.myapplication.Exceptions.NotAnEquation;

import java.util.EmptyStackException;

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
    Calculator calculator;
    Modes calculatorMode = Modes.CALCULATING;

    //TODO: add system of equations function
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userGuide = findViewById(R.id.userGuide);
        numInput = findViewById(R.id.numInput);
        calculate = findViewById(R.id.calculate);
        result = findViewById(R.id.result);

        calculator = Calculator.getCalculator();

        userGuide.setText(calculator.listAllSpecialOps());
        result.setText(R.string.resultPrompt);
        numInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = numInput.getText().toString();
//                if (eqWithVar(input)) calculatorMode = Modes.EQ_SOLVING;
                if (input.indexOf('=') != -1) calculatorMode = Modes.EQ_SOLVING;
                else calculatorMode = Modes.CALCULATING;
                calculate.setText(
                        calculatorMode == Modes.EQ_SOLVING ? R.string.Solve : R.string.calculate
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        calculate.setOnClickListener(v -> {
            String input = numInput.getText().toString();
            try {
                Double calcResult;
                if (calculatorMode.isCalculateMode())
                    result.setText(String.format("%.5f", calculator.calculate(input)));
                else
                result.setText(calculator.solveAll(input));
            } catch (IllegalOperator | NoSolution error) {
                String message = error.getMessage();
                result.setText(message);
            } catch (EmptyStackException emptyStackException){
                result.setText(R.string.wrongArithmatic);
            } catch (NotAnEquation notAnEquation) {
                result.setText(notAnEquation.getMessage());
                notAnEquation.printStackTrace();
            }
        });
    }

    public void announce(String announcement){

    }
}