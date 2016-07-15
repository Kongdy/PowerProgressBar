package com.project.kongdy.powerprogressbar.View;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;

/**
 * @author kongdy
 *         on 2016/7/15
 * 强大的圆形progressbar，可在多个款式里面进行切换,
 * 拥有一个默认style
 * <h1>
 *     尽可能进行高DIY化开发
 * </h1>
 */
public class PowerProgressBar extends View {

    /** 主要画笔 */
    private Paint mainPaint;
    private Paint fullPaint;
    private TextPaint labelPaint;
    private Paint rulerPaint;

    /** 进度条宽度 */
    private int progressWidth = -1;

    private Path progressClipPath;
    private Path progressFullPath;

    private int mWidth;
    private int mHeight;
    private int mRadius;
    private int centerX;
    private int centerY;

    private int progressColor;
    private int progressBgColor;
    private int rulerColor;
    private int labelColor;

    private boolean setProgressColor = false;
    private boolean setProgressBgColor = false;
    private boolean setRulerColor = false;
    private boolean setLabelColor = false;

    /** 外部标注宽度 */
    private int labelWidth = 0;
    private float labelSize = -1;

    /** 中间部分的自定义区域 */
    private View centerView;

    private float progressValue = -1;
    private float maxAngle = -1;
    /** 当前度数，便于动画 */
    private float currentValue = 0;
    /** 开始动画持续时间 */
    private long startAnimalTime;
    /** 是否播放开始动画 */
    private boolean startAnimal = true;

    private RectF progressRectF;

    private Runnable startAniamlRunnable;

    private SparseIntArray labelList;
    private SparseArray<Point> labelCoord;

    public PowerProgressBar(Context context) {
        super(context);
        init();
    }

    public PowerProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PowerProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        mainPaint = new Paint();
        fullPaint = new Paint();
        rulerPaint = new Paint();
        labelPaint = new TextPaint();
        progressClipPath = new Path();
        progressFullPath = new Path();

        // 抗锯齿
        mainPaint.setAntiAlias(true);
        fullPaint.setAntiAlias(true);
        labelPaint.setAntiAlias(true);
        rulerPaint.setAntiAlias(true);

        // 防抖动
        mainPaint.setDither(true);
        fullPaint.setDither(true);
        labelPaint.setDither(true);
        rulerPaint.setDither(true);

        // 滤波处理
       /* 图像滤波，即在尽量保留图像细节特征的条件下对目标图像的噪声进行抑制，
        是图像预处理中不可缺少的操作，其处理效果的好坏将直接影响到后续图像处理和分析的有效性和可靠性*/
        mainPaint.setFilterBitmap(true);
        fullPaint.setFilterBitmap(true);
        labelPaint.setFilterBitmap(true);

        // 文字自像素处理，有助于文字在LCD屏幕的显示效果
        //mainPaint.setSubpixelText(true);
        labelPaint.setSubpixelText(true);

        mainPaint.setStyle(Paint.Style.STROKE);
        fullPaint.setStyle(Paint.Style.STROKE);
        rulerPaint.setStyle(Paint.Style.STROKE);

        // 画笔结合处样式
        mainPaint.setStrokeJoin(Paint.Join.ROUND);
        fullPaint.setStrokeJoin(Paint.Join.ROUND);

        // 画笔端头样式
        mainPaint.setStrokeCap(Paint.Cap.ROUND);
        fullPaint.setStrokeCap(Paint.Cap.ROUND);

        mainPaint.setStrokeWidth(getProgressWidth());
        fullPaint.setStrokeWidth(getProgressWidth());
        rulerPaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP,1)); // 赋值1dp的宽度

        labelPaint.setTextSize(getLabelSize());

        mainPaint.setColor(getProgressColor());
        fullPaint.setColor(getProgressBgColor());
        rulerPaint.setColor(getRulerColor());
        labelPaint.setColor(getLabelColor());

        // 头发的特技
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(50,BlurMaskFilter.Blur.SOLID);
        mainPaint.setMaskFilter(blurMaskFilter);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null); // 启用软件加速，防止在4.0以上不产生效果

        labelList = new SparseIntArray();
        labelCoord = new SparseArray<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.saveLayer(0,0,getMeasuredWidth(),getMeasuredHeight(),mainPaint,Canvas.ALL_SAVE_FLAG);

        if(centerView != null) {
            centerView.draw(canvas);
        }

        canvas.drawPath(progressFullPath,fullPaint);
        canvas.drawPath(progressClipPath,mainPaint);

        for (int i = 0;i < labelList.size();i++) {
            canvas.drawText(labelList.get(i)+"",labelCoord.get(i).x,labelCoord.get(i).y,labelPaint);
        }

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        centerX = mWidth/2;
        centerY = mHeight/2;
        final int minDistance = centerX < centerY?centerX:centerY;
        mRadius = (int)(9*(minDistance/10)-labelWidth-mainPaint.getStrokeWidth());
        resetPath();
    }

    private void resetPath() {
        progressRectF = new RectF(centerX-mRadius,centerY-mRadius,centerX+mRadius,
               centerY+mRadius);
        float startAngle = ((100f-getProgressValue())/100f)*360f*1.5f;
        // 清除路径
        progressClipPath.reset();
        progressClipPath.addArc(progressRectF,startAngle,(currentValue/100f)*getMaxAngle());
        progressFullPath.reset();
        progressFullPath.addArc(progressRectF,startAngle,getMaxAngle());
        if(progressWidth == -1) {
            setProgressWidth(mRadius/5);
        }
        // 计算label坐标
        float unitAngle = getMaxAngle()/(labelList.size()+1);
        float labelRadius = mRadius+mainPaint.getStrokeWidth();
        for (int i = 0;i < labelList.size();i++) {
            Point point = new Point();

            float angle = unitAngle*(i+1)+startAngle;
            point.y = (int) (centerY + Math.sin(Math.toRadians(angle))*labelRadius);
            point.x = (int) (centerX+Math.cos(Math.toRadians(angle))*labelRadius);

            labelCoord.put(i,point);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void reDraw() {
        resetPath();
        if(Build.VERSION.SDK_INT > 15) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }


    public int getProgressWidth() {
        if(progressWidth != -1) {
            return progressWidth;
        } else {
            return 50;
        }
    }

    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
        mainPaint.setStrokeWidth(progressWidth);
        fullPaint.setStrokeWidth(progressWidth);
        invalidate();
    }

    private float getRawSize(int unit,int value) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit,value,displayMetrics);
    }


    private void animalToStart() {

        final long startTime = getStartAnimalTime();
        ValueAnimator animator = ValueAnimator.ofFloat(0f,progressValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentValue = (float) valueAnimator.getAnimatedValue();
                reDraw();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                startAniamlRunnable = null;
            }
            @Override
            public void onAnimationCancel(Animator animator) {
                startAniamlRunnable = null;
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        // 400毫秒的开启延迟
        animator.setStartDelay(400);
        animator.setDuration(startTime);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 控件发生改变，执行开始动画
        animalToStart();
    }

    /**
     * 默认270度
     * @return
     */
    public float getMaxAngle() {
        if(maxAngle == -1)
            this.maxAngle = 270f;
        return maxAngle;
    }

    /**
     * 设置最大进度
     * @param maxAngle
     */
    public void setMaxAngle(float maxAngle) {
        if(maxAngle > 360f) {
            maxAngle = 360f;
        }
        if(maxAngle < 0f) {
            maxAngle = 0f;
        }
        this.maxAngle = maxAngle;
        invalidate();
    }

    /**
     * 默认进度显示到75%
     * @return
     */
    public float getProgressValue() {
        if(progressValue == -1)
             this.progressValue = -75;
        return progressValue;
    }

    /**
     * 设置当前进度
     * @param progressValue
     */
    public void setProgressValue(float progressValue) {
        if(progressValue > 100) {
            progressValue = 100;
        }
        if(progressValue < 0) {
            progressValue = 0;
        }
        this.progressValue = progressValue;

    }


    public int getProgressBgColor() {
        if(!setProgressBgColor)
             this.progressBgColor = Color.rgb(238,238,238);
        return progressBgColor;
    }


    public void setProgressBgColor(int progressBgColor) {
        this.progressBgColor = progressBgColor;
        setProgressBgColor = true;
    }

    public int getProgressColor() {
        if(!setProgressColor)
            this.progressColor = Color.rgb(155,225,103);
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        setProgressColor = true;
    }

    public View getCenterView() {
        return centerView;
    }

    /**
     * 设置中间区域的布局
     * @param centerView
     */
    public void setCenterView(View centerView) {
        this.centerView = centerView;
        invalidate();
    }

    public long getStartAnimalTime() {
        if(startAnimalTime <= 0){
            this.startAnimalTime = 1000;
        }
        return startAnimalTime;
    }

    public void setStartAnimalTime(long startAnimalTime) {
        this.startAnimalTime = startAnimalTime;
    }

    public boolean isStartAnimal() {
        return startAnimal;
    }

    public void setStartAnimal(boolean startAnimal) {
        this.startAnimal = startAnimal;
    }

    public int getRulerColor() {
        if(!setRulerColor) {
            rulerColor = Color.rgb(199,199,199);
        }
        return rulerColor;
    }

    public void setRulerColor(int rulerColor) {
        this.rulerColor = rulerColor;
        setRulerColor = true;
    }

    public int getLabelColor() {
        if(!setLabelColor) {
            labelColor = Color.rgb(199,199,199);
        }
        return labelColor;
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        setLabelColor = true;
    }

    public float getLabelSize() {
        if(labelSize == -1) {
            labelSize = getRawSize(TypedValue.COMPLEX_UNIT_SP,12);
        }
        return labelSize;
    }

    public void setLabelSize(int labelSize) {
        this.labelSize = labelSize;
    }

    /**
     * 设置label
     * @param minValue
     * @param maxValue
     * @param valueStep
     */
    public void setLabel(int minValue,int maxValue,int valueStep) {
        if(maxValue < minValue) {
            maxValue = minValue;
        }
        if((maxValue - minValue) < valueStep) {
            valueStep = maxValue - minValue;
        }
        int i = 0;
        int tempValue = minValue;
        while(tempValue <= maxValue) {
            labelList.put(i,tempValue);
            tempValue = tempValue+valueStep;
            i++;
        }
    }

    /**
     * 设置label
     * @param array
     */
    public void setLabel(SparseIntArray array) {
        this.labelList = array;
    }
}
