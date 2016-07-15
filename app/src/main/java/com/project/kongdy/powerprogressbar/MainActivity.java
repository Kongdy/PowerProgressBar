package com.project.kongdy.powerprogressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.project.kongdy.powerprogressbar.View.PowerProgressBar;

public class MainActivity extends AppCompatActivity {

    private PowerProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (PowerProgressBar) findViewById(R.id.progressBar);

        progressBar.setProgressValue(75f);
    }



    @Override
    protected void onStart() {
        super.onStart();
    }
}
