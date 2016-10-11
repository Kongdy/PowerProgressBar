package com.project.kongdy.powerprogressbar.View;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @author kongdy
 *         on 2016/8/12
 *         进度条的中间布局
 */
public class PowerProgressCenterView extends RelativeLayout {

    private Path clipPath;
    private float radius = 5000;

    public PowerProgressCenterView(Context context) {
        super(context);
        init();
    }

    public PowerProgressCenterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PowerProgressCenterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PowerProgressCenterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        clipPath = new Path();
    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        clipPath.reset();
        clipPath.addCircle(getMeasuredWidth()/2, getMeasuredHeight()/2, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        return super.drawChild(canvas, child, drawingTime);
    }

    public void setClipRadius(float radius) {
        this.radius = radius;
    }
}
