package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Exceptions.IllegalOperator;

import java.util.EmptyStackException;

public class MainActivity extends AppCompatActivity {

    TextView userGuide;
    EditText numInput;
    Button calculate;
    TextView result;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userGuide = findViewById(R.id.userGuide);
        numInput = findViewById(R.id.numInput);
        calculate = findViewById(R.id.calculate);
        result = findViewById(R.id.result);

        //Experimental calculator
        Calculator calculator = Calculator.getCalculator();

        userGuide.setText(calculator.listAllSpecialOps());
        result.setText(String.format("%-6s", "result"));

        calculate.setOnClickListener(v -> {
            String input = numInput.getText().toString();
            try {
                Double calcResult = calculator.calculate(input);
                result.setText(String.format("%.7f", calcResult));
            } catch (IllegalOperator illegalOperator) {
                String message = illegalOperator.getMessage();
                result.setText(message);
            } catch (EmptyStackException emptyStackException){
                result.setText(String.format("%-16s", "Wrong arithmetic"));
            }
        });
    }
}