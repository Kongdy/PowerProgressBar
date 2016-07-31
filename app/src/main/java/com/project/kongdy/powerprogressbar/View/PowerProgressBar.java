package com.project.kongdy.powerprogressbar.View;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

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
    private Paint externalPaint;

    /** 进度条宽度 */
    private int progressWidth = -1;

    private Path progressClipPath;
    private Path progressFullPath;

    private int mWidth;
    private int mHeight;
    private int mRadius;
    private int centerX;
    private int centerY;

    private int[] progressColor;
    private int progressBgColor;
    private int rulerColor;
    private int labelColor;

    private boolean setProgressBgColor = false;
    private boolean setRulerColor = false;
    private boolean setLabelColor = false;
    private boolean showHalo = true;
    private boolean externalCircle = false; // 是否增加一个外圈

    private ProgressStyle progressStyle;// 默认实线

    /** 外部标注宽度 */
    private float labelSize = -1;

    /** 中间部分的自定义区域 */
    private View centerView;

    private float progressValue = -1;
    private float maxAngle = -1;
    /** 当前度数，便于动画 */
    private float currentValue = 0;

    private RectF progressRectF;
    /** 基点角度，计算过程中，将根据该值产生基点的角度偏移，默认90度 */
    private float BasePointAngle = 90f;
    /** 设置进度条比率 */
    private double progressWidthRate = -1;

    private float startAngle;

    // 开场动画
    private ValueAnimator animator;

    private SparseIntArray labelList;
    private SparseArray<Point> labelCoord;
    private SparseArray<Point> rulerStartCoord;
    private SparseArray<Point> rulerEndCoord;
    private SparseIntArray bgColor;

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

        setWillNotDraw(false);
        mainPaint = new Paint();
        fullPaint = new Paint();
        rulerPaint = new Paint();
        labelPaint = new TextPaint();
        progressClipPath = new Path();
        progressFullPath = new Path();

        labelPaint.setTextAlign(Paint.Align.CENTER);

        mainPaint.setStyle(Paint.Style.STROKE);
        fullPaint.setStyle(Paint.Style.STROKE);
        rulerPaint.setStyle(Paint.Style.STROKE);


        labelList = new SparseIntArray();
        labelCoord = new SparseArray<Point>();
        rulerStartCoord = new SparseArray<Point>();
        rulerEndCoord = new SparseArray<Point>();

        OpenHighQuality(true);
    }

    // TODO: 2016/7/18  need measure DASHEDLINE effect padding distances
    private void initProperty() {
        if(getProgressStyle() == ProgressStyle.DASHEDLINE) {
            PathEffect pathEffect = new DashPathEffect(new float[]{15,15,15,15},0);
            mainPaint.setPathEffect(pathEffect);
            fullPaint.setPathEffect(pathEffect);
        } else if(getProgressStyle() == ProgressStyle.FILLLINE) {
            // 画笔结合处样式
            mainPaint.setStrokeJoin(Paint.Join.BEVEL);
            fullPaint.setStrokeJoin(Paint.Join.BEVEL);

            // 画笔端头样式
            mainPaint.setStrokeCap(Paint.Cap.ROUND);
            fullPaint.setStrokeCap(Paint.Cap.ROUND);
        } else if(getProgressStyle() == ProgressStyle.CURSOR) {
            mainPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            // 画笔结合处样式
            mainPaint.setStrokeJoin(Paint.Join.BEVEL);
            fullPaint.setStrokeJoin(Paint.Join.BEVEL);
        }

        fullPaint.setStrokeWidth(getProgressWidth());
        rulerPaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP,1)); // 赋值1dp的宽度


        labelPaint.setTextSize(getLabelSize());

        fullPaint.setColor(getProgressBgColor());
        rulerPaint.setColor(getRulerColor());
        labelPaint.setColor(getLabelColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.saveLayer(0,0,getMeasuredWidth(),getMeasuredHeight(),mainPaint,Canvas.ALL_SAVE_FLAG);

        if(externalPaint != null && externalCircle) {
            canvas.drawCircle(centerX,centerY,mRadius+mainPaint.getStrokeWidth(),externalPaint);
        }
        if(progressStyle == ProgressStyle.CURSOR) {
            float cursorAngle = startAngle+1;
            float growAngle = getMaxAngle()/bgColor.size()-2;
            for (int i = 0;i < bgColor.size();i++) {
                progressFullPath.reset();
                fullPaint.setColor(bgColor.get(i));
                progressFullPath.addArc(progressRectF,cursorAngle,growAngle);
                canvas.drawPath(progressFullPath,fullPaint);
                cursorAngle = cursorAngle+growAngle+2;
            }
        } else {
            canvas.drawPath(progressFullPath,fullPaint);
        }
        canvas.drawPath(progressClipPath,mainPaint);

        for (int i = 0;i < labelList.size();i++) {
            // 画label
            canvas.drawText(labelList.get(i)+"",labelCoord.get(i).x,labelCoord.get(i).y,labelPaint);
            // 画刻度
            canvas.drawLine(rulerStartCoord.get(i).x,rulerStartCoord.get(i).y,
                    rulerEndCoord.get(i).x,rulerEndCoord.get(i).y,rulerPaint);
        }
        drawCenterView(canvas);
        canvas.restore();
    }


    private void resetPath() {
        if(getProgressColor().length < 2) {
            mainPaint.setColor(getProgressColor()[0]);
        } else {
            SweepGradient sweepGradient = new SweepGradient(centerX,centerY,getProgressColor(),null);
            Matrix rotateMartix = new Matrix();
            rotateMartix.setRotate(90f,centerX,centerY);
            sweepGradient.setLocalMatrix(rotateMartix);
            mainPaint.setShader(sweepGradient);
        }

        if(progressWidth == -1) {
            setProgressWidth((int) (mRadius*getProgressWidthRate()));
        } else {
            setProgressWidth(progressWidth);
        }

        progressRectF = new RectF(centerX-mRadius,centerY-mRadius,centerX+mRadius,
               centerY+mRadius);
        startAngle  = (360f-getMaxAngle())/2+getBasePointAngle();
        float currentAngle = startAngle+(currentValue/100f)*getMaxAngle();

        // 清除路径
        progressClipPath.rewind();
        if(getProgressStyle() == ProgressStyle.CURSOR) {
            mainPaint.setStrokeWidth(1);
            progressClipPath.moveTo((float)(centerX+Math.cos(Math.toRadians(currentAngle-10))*(mRadius-fullPaint.getStrokeWidth()/2)),
                    (float) (centerY+Math.sin(Math.toRadians(currentAngle-10))*(mRadius-fullPaint.getStrokeWidth()/2)));
            progressClipPath.lineTo((float)(centerX+Math.cos(Math.toRadians(currentAngle))*(mRadius+fullPaint.getStrokeWidth()/6)),
                    (float) (centerY+Math.sin(Math.toRadians(currentAngle))*(mRadius+fullPaint.getStrokeWidth()/6)));
            progressClipPath.lineTo((float)(centerX+Math.cos(Math.toRadians(currentAngle+10))*(mRadius-fullPaint.getStrokeWidth()/2)),
                    (float) (centerY+Math.sin(Math.toRadians(currentAngle+10))*(mRadius-fullPaint.getStrokeWidth()/2)));
            progressClipPath.close();
        } else {
            progressClipPath.addArc(progressRectF,startAngle,(currentValue/100f)*getMaxAngle());
        }

        progressFullPath.rewind();
        progressFullPath.addArc(progressRectF,startAngle,getMaxAngle());

        float unitAngle = getMaxAngle()/(labelList.size()+1);
        float labelRadius = mRadius+mainPaint.getStrokeWidth();
        labelCoord.clear();
        rulerStartCoord.clear();
        rulerEndCoord.clear();
        for (int i = 0;i < labelList.size();i++) {
            // 计算label坐标
            Point point = new Point();
            float angle = unitAngle*(i+1)+startAngle;
            point.y = (int) (centerY + Math.sin(Math.toRadians(angle))*labelRadius);
            point.x = (int) (centerX+Math.cos(Math.toRadians(angle))*labelRadius);
            labelCoord.put(i,point);
            // 计算ruler坐标偏移值,start
            float rulerRadius = mRadius+mainPaint.getStrokeWidth()/2;
            Point point1 = new Point();
            point1.x = (int) (centerX+Math.cos(Math.toRadians(angle))*rulerRadius);
            point1.y = (int) (centerY + Math.sin(Math.toRadians(angle))*rulerRadius);
            rulerStartCoord.put(i,point1);
            // end
            Point point2 = new Point();
            point2.x = (int) (centerX+Math.cos(Math.toRadians(angle))*(rulerRadius-mainPaint.getStrokeWidth()*2/3));
            point2.y = (int) (centerY + Math.sin(Math.toRadians(angle))*(rulerRadius-mainPaint.getStrokeWidth()*2/3));
            rulerEndCoord.put(i,point2);
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

    /**
     * 开关高画质
     * @param flag
     */
    public void OpenHighQuality(boolean flag) {
        // 抗锯齿
        mainPaint.setAntiAlias(flag);
        fullPaint.setAntiAlias(flag);
        labelPaint.setAntiAlias(flag);
        rulerPaint.setAntiAlias(flag);

        // 防抖动
        mainPaint.setDither(flag);
        fullPaint.setDither(flag);
        labelPaint.setDither(flag);
        rulerPaint.setDither(flag);

        // 滤波处理
       /* 图像滤波，即在尽量保留图像细节特征的条件下对目标图像的噪声进行抑制，
        是图像预处理中不可缺少的操作，其处理效果的好坏将直接影响到后续图像处理和分析的有效性和可靠性*/
        mainPaint.setFilterBitmap(flag);
        fullPaint.setFilterBitmap(flag);
        labelPaint.setFilterBitmap(flag);

        // 文字自像素处理，有助于文字在LCD屏幕的显示效果
        //mainPaint.setSubpixelText(true);
        labelPaint.setSubpixelText(flag);
    }


    public int getProgressWidth() {
        return progressWidth;
    }

    /**
     * 设置详进度条宽度的精确宽度
     * @param progressWidth
     */
    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
        mainPaint.setStrokeWidth(progressWidth);
        fullPaint.setStrokeWidth(progressWidth);
        invalidate();
    }

    public double getProgressWidthRate() {
        if(progressWidthRate < 0) {
            progressWidthRate = 0.2d;
        }
        return progressWidthRate;
    }

    public void setProgressWidthRate(double progressWidthRate) {
        this.progressWidthRate = progressWidthRate;
    }

    private float getRawSize(int unit, int value) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit,value,displayMetrics);
    }

    public void animalToStart(long time) {
        if(animator == null) {
            animator = ValueAnimator.ofFloat(0f,progressValue);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    currentValue = (Float) valueAnimator.getAnimatedValue();
                    reDraw();
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                    startAnimalEnd();
                }
                @Override
                public void onAnimationCancel(Animator animator) {
                    startAnimalEnd();
                }
                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            // 400毫秒的开启延迟
            animator.setStartDelay(200);
            animator.setDuration(time);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.start();
        }
    }

    private void startAnimalEnd() {
        animator = null;
        if(showHalo) {
            // 头发的特技
            setLayerType(View.LAYER_TYPE_SOFTWARE,null); // 启用软件加速，防止在4.0以上不产生效果
            ValueAnimator animator1 = ValueAnimator.ofFloat(1f,50f);
            animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Float value = (Float) valueAnimator.getAnimatedValue();
                    BlurMaskFilter blurMaskFilter = new BlurMaskFilter(value,BlurMaskFilter.Blur.SOLID);
                    mainPaint.setMaskFilter(blurMaskFilter);
                    invalidate();
                }
            });
            animator1.setDuration(300);
            animator1.start();
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initProperty();
        // 根据度数的改变进度条也会相应的改变大小
        mWidth = w-getPaddingLeft()-getPaddingRight();
        mHeight = h-getPaddingTop()-getPaddingBottom();
        centerX = mWidth/2;
        getProgressWidthRate();
        if(externalCircle) {
            centerY = mHeight/2;
            final int minDistance = centerX < centerY?centerX:centerY;
            final int mRadiusOffset = progressWidth == -1? (int) (minDistance * progressWidthRate / 2) :progressWidth/2;
            mRadius = (int) (minDistance-2*mRadiusOffset-externalPaint.getStrokeWidth());
        } else {
            double tempDis = 1- Math.cos(Math.toRadians((360f-getMaxAngle())/2d));
            int tempRadius = mHeight/2;
            centerY = (int) (tempRadius+tempRadius*tempDis);
            final int minDistance = centerX < centerY?centerX:centerY;
            final int mRadiusOffset = progressWidth == -1? (int) (minDistance * progressWidthRate / 2) :progressWidth/2;
            final int mRadiusOffset2 = (int) Math.abs((tempRadius+mRadiusOffset)*(1-tempDis)-tempRadius*(1-tempDis));
            final int mRadiusOffset3 = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP,1); // offset default Padding
            mRadius = minDistance-mRadiusOffset-mRadiusOffset2-mRadiusOffset3;
        }
        if(labelList.size() > 0) {
            mRadius = (int) (mRadius-labelPaint.getFontSpacing());
        }
        resetPath();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(centerView != null) {
            centerView.measure(MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
        }
    }

    /**
     * 计算偏移值，来移动centerView 的位置
     */
    private void drawCenterView(Canvas canvas) {
        if(centerView != null) {
            float offsetX = centerX-mRadius+mainPaint.getStrokeWidth();
            float offsetY = centerY-mRadius+mainPaint.getStrokeWidth();

            canvas.translate(offsetX,offsetY);
            centerView.draw(canvas);
            canvas.translate(-offsetX,-offsetY);
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if(centerView != null) {
//            centerView.layout((int)(mWidth-mRadius+mainPaint.getStrokeWidth()),(int)(mHeight-mRadius+mainPaint.getStrokeWidth())
//                    ,(int)(centerX+mRadius-mainPaint.getStrokeWidth()),(int)(centerY+mRadius-mainPaint.getStrokeWidth()));
 //           centerView.layout(0,0,mWidth,mHeight);
            centerView.layout((int)(centerX-mRadius+mainPaint.getStrokeWidth()),(int)(centerY-mRadius+mainPaint.getStrokeWidth()),
                    (int)(centerX+mRadius-mainPaint.getStrokeWidth()),(int)(centerY+mRadius-mainPaint.getStrokeWidth()));
        }
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


    public void setProgressBgColor(int... progressBgColor) {
        if(progressBgColor.length > 0&& progressBgColor.length <= 1)
            this.progressBgColor = progressBgColor[0];
        else
        {   bgColor = new SparseIntArray();
            for (int i = 0;i < progressBgColor.length;i++) {
                bgColor.put(i,progressBgColor[i]);
            }
        }
        setProgressBgColor = true;
    }

    public int[] getProgressColor() {
        if(progressColor == null)
            progressColor = new int[]{Color.rgb(155,225,103)};
        return progressColor;
    }

    public void setProgressColor(int[] progressColor) {
        this.progressColor = progressColor;
    }

    public View getCenterView() {
        return centerView;
    }

    /**
     * 设置中间区域的布局
     * @param centerView
     * 由于时间问题，改方法未完善，使用的时候会造成不必要的布局错误
     */
    @Deprecated
    public void setCenterView(View centerView) {
        if(centerView.getParent() != null) {
            ((ViewGroup)centerView.getParent()).removeView(centerView);
        }
        this.centerView = centerView;
        invalidate();
    }


    public int getRulerColor() {
        if(!setRulerColor) {
            rulerColor = Color.rgb(220,220,220);
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
    public PowerProgressBar setLabel(int minValue, int maxValue, int valueStep) {
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
        return null;
    }

    /**
     * 设置label
     * @param array
     */
    public void setLabel(SparseIntArray array) {
        this.labelList = array;
    }

    public boolean isShowHalo() {
        return showHalo;
    }

    /**
     * 是否显示光晕
     * @param showHalo
     */
    public void setShowHalo(boolean showHalo) {
        this.showHalo = showHalo;
    }

    public ProgressStyle getProgressStyle() {
        if(progressStyle == null) {
            progressStyle = ProgressStyle.FILLLINE;
        }
        return progressStyle;
    }

    public void setProgressStyle(ProgressStyle progressStyle) {
        this.progressStyle = progressStyle;
        invalidate();
    }

    public float getBasePointAngle() {
        return BasePointAngle;
    }

    public void setBasePointAngle(float basePointAngle) {
        BasePointAngle = basePointAngle;
    }

    public boolean isExternalCircle() {
        return externalCircle;
    }

    public void setExternalCircle(boolean externalCircle,int color) {
        this.externalCircle = externalCircle;
        externalPaint = new Paint();
        externalPaint.setStyle(Paint.Style.STROKE);
        externalPaint.setStrokeWidth(getRawSize(TypedValue.COMPLEX_UNIT_DIP,1));
        externalPaint.setColor(color);
    }

    /**
     * 进度条样式
     */
    public static enum ProgressStyle{
        /** 虚线 */
        DASHEDLINE,
        /** 实线 */
        FILLLINE,
        /** 三角指针 */
        CURSOR,
    }

}
