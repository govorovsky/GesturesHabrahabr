package ru.suvrik.gestureshabrahabr;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ApplicationView(this));
    }
}