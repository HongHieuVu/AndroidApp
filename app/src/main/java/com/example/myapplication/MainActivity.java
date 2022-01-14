package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.CrossServiceActions.Calculate;
import com.example.myapplication.CrossServiceActions.Autofill;
import com.example.myapplication.CrossServiceActions.CreateEditText;
import com.example.myapplication.CrossServiceActions.GetCalculatorInstruction;
import com.example.myapplication.CrossServiceActions.SolveEquation;

public class MainActivity extends AppCompatActivity {

    LinearLayout linearLayout;

    TextView userGuide;
    EditText numInput;
    public Button calculate, moreEquation;
    TextView result;

    Modes calculatorMode = Modes.calculatorMode;

    //TODO: add system of equations function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        linearLayout = findViewById(R.id.linearLayout1);
        userGuide = findViewById(R.id.userGuide);
        numInput = findViewById(R.id.numInput);
        moreEquation = findViewById(R.id.moreEquation);
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

                //checks whether input is now an equation to change mode
                if (input.indexOf('=') != -1) calculatorMode = Modes.EQ_SOLVING;
                else calculatorMode = Modes.CALCULATING;
                calculate.setText(calculatorMode.getButtonMsg());

                //autofill operator for user, must be on this thread
                String newText = new Autofill(input).getResult();
                if (!newText.equals(input)) {
                    numInput.setText(newText);
                    numInput.setSelection(newText.length()); //set cursor
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        moreEquation.setOnClickListener(v -> {
            new CreateEditText(linearLayout, this);
        });

        calculate.setOnClickListener(v -> {
            String input = numInput.getText().toString();
            StringBuilder res = new StringBuilder();

            //Runs on another thread so that it won't stall UI thread
            new Thread(() -> {

                //calculate with corresponding mode
                if (calculatorMode.isCalculateMode())
                    res.append(new Calculate(input).getResult());
                else
                    res.append(new SolveEquation(input).getResult());

                //update result view
                runOnUiThread(() -> {
                    if (res.length() != 0) result.setText(res.toString());
                });
            }).start();
        });
    }
}