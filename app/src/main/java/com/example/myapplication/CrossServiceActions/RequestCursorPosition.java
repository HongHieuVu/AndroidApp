package com.example.myapplication.CrossServiceActions;

import android.widget.EditText;

public class RequestCursorPosition extends Actions{
    public RequestCursorPosition(EditText editText, int newPos){
        setResult(() -> {
            editText.setSelection(newPos);
            return null;
        });
    }
}
