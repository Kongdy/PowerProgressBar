package com.project.kongdy.powerprogressbar;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.project.kongdy.powerprogressbar.View.PowerProgressBar;

public class MainActivity extends AppCompatActivity {

    private PowerProgressBar progressBar1;
    private PowerProgressBar progressBar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar1 = (PowerProgressBar) findViewById(R.id.progressBar1);
        progressBar2 = (PowerProgressBar) findViewById(R.id.progressBar2);

        // style1
        progressBar1.setLabel(34,45,1);
        progressBar1.setMaxAngle(270);
        progressBar1.setProgressValue(75f);
        progressBar1.setProgressColor(new int[] {Color.rgb(71,172,216),
                Color.rgb(192,228,64),Color.rgb(254,145,148)});
        progressBar1.setProgressStyle(PowerProgressBar.ProgressStyle.FILLLINE);

        // style2
        progressBar2.setMaxAngle(360);
        progressBar2.setProgressValue(100f);
        progressBar2.setProgressStyle(PowerProgressBar.ProgressStyle.DASHEDLINE);

    }



    @Override
    protected void onStart() {
        super.onStart();
    }
}
