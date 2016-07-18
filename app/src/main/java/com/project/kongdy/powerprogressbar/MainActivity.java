package com.project.kongdy.powerprogressbar;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.project.kongdy.powerprogressbar.View.PowerProgressBar;

public class MainActivity extends AppCompatActivity {

    private PowerProgressBar progressBar1;
    private PowerProgressBar progressBar2;
    private PowerProgressBar progressBar3;
    private PowerProgressBar progressBar4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = LayoutInflater.from(this).inflate(R.layout.layout,null);

        progressBar1 = (PowerProgressBar) findViewById(R.id.progressBar1);
        progressBar2 = (PowerProgressBar) findViewById(R.id.progressBar2);
        progressBar3 = (PowerProgressBar) findViewById(R.id.progressBar3);
        progressBar4 = (PowerProgressBar) findViewById(R.id.progressBar4);

        // style1
        progressBar1.setLabel(34,45,1); // 设置刻度标签和，最小值，最大值，步长
        progressBar1.setMaxAngle(270);// 设置慢进度最大角度
        progressBar1.setProgressValue(75f); // 设置当前进度
        // 设置进度条颜色，颜色为多个的话，会变成角度渐变色
        progressBar1.setProgressColor(new int[] {Color.rgb(71,172,216),
                Color.rgb(192,228,64),Color.rgb(254,145,148)});
        progressBar1.setProgressStyle(PowerProgressBar.ProgressStyle.FILLLINE); // 设置进度条样式
        progressBar1.OpenHighQuality(false); // 关闭高画质
        progressBar1.setCenterView(view);

        // style2
        progressBar2.setMaxAngle(360);
        progressBar2.setProgressValue(75f);
        progressBar2.setProgressStyle(PowerProgressBar.ProgressStyle.DASHEDLINE);
        progressBar2.setBasePointAngle(270f); //使基点往前挪270度,默认90
        progressBar2.OpenHighQuality(false);
        progressBar2.setCenterView(view);

        // style3
        progressBar3.setMaxAngle(180);
        progressBar3.setProgressValue(75);
        progressBar3.setProgressStyle(PowerProgressBar.ProgressStyle.CURSOR);
        progressBar3.OpenHighQuality(true);
        progressBar3.setProgressWidthRate(0.4); // 设置进度条宽度半径占比，1则满圆
        // 设置多个颜色的话，会自动分割成多个扇形,每个扇形中间隔两度
        progressBar3.setProgressBgColor(Color.rgb(131,151,232),Color.rgb(107,202,220),Color.rgb(157,222,106),
                Color.rgb(255,234,0),Color.rgb(252,181,3),Color.rgb(254,145,147));
        progressBar3.setProgressColor(new int[]{Color.WHITE});
        progressBar3.setCenterView(view);

        // style4
        progressBar4.setMaxAngle(270);
        progressBar4.setProgressStyle(PowerProgressBar.ProgressStyle.FILLLINE);
        progressBar4.OpenHighQuality(false);
        progressBar4.setProgressValue(75f);
        progressBar4.setProgressColor(new int[]{Color.rgb(155,225,103)});
        progressBar4.setProgressWidthRate(0.15);
        progressBar4.setExternalCircle(true,Color.rgb(238,238,238)); // 增加一个外环
        progressBar4.setCenterView(view);

    }



    @Override
    protected void onStart() {
        super.onStart();
    }
}
