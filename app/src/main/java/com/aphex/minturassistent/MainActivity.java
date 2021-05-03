package com.aphex.minturassistent;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import com.aphex.minturassistent.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(activityMainBinding.getRoot());
    }
}