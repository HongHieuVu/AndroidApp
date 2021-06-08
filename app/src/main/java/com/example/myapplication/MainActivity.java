package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Exceptions.IllegalOperator;

public class MainActivity extends AppCompatActivity {

    EditText numInput;
    Button calculate;
    TextView result;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numInput = findViewById(R.id.numInput);
        calculate = findViewById(R.id.calculate);
        result = findViewById(R.id.result);

        result.setText(String.format("%-6s", "result"));

        calculate.setOnClickListener(v -> {
            Calculator calculator = Calculator.getCalculator();
            String input = numInput.getText().toString();
            try {
                Double calcResult = calculator.calculate(input);
                result.setText(String.format("%.7f", calcResult));
            } catch (IllegalOperator illegalOperator) {
                String message = illegalOperator.getMessage();
                result.setText(message);
            }
        });
    }
}