package com.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hu.annotation.SuperSDCardUtil;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SuperSDCardUtil.getSDCardDataPath();
    }
}
