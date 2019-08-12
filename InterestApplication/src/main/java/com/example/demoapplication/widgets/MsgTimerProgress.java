package com.example.demoapplication.widgets;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.example.demoapplication.R;

import java.text.DecimalFormat;

/**
 * author : ZFM
 * date   : 2019/7/2614:47
 * desc   : 圆形进度条带渐变色
 * version: 1.0
 */
public class MsgTimerProgress extends View {


    private RectF mWheelRect = new RectF();
    private Paint mDefaultWheelPaint;
    private Paint mFinishWheelPaint;
    private float mCircleStrokeWidth;
    private float mSweepAnglePer;
    private float mPercent;
    private long mStepNum, mCurrStepNum;
    private float pressExtraStrokeWidth;
    private BarAnimation mAnim;
    private int mMaxStepNum;// 默认最大步数
    private DecimalFormat mDecimalFormat = new DecimalFormat("#.0");// 格式为保留小数点后一位
    public static String GOAL_STEP;
    public static String PERCENT;


    private RectF mBitmapRect = new RectF();
    private Bitmap mBitmap;
    private Paint mBitmapPaint;

    public MsgTimerProgress(Context context) {
        super(context);
        init(null, 0);
    }

    public MsgTimerProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MsgTimerProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        mFinishWheelPaint = new Paint();
        mFinishWheelPaint.setColor(Color.rgb(100, 113, 205));
        mFinishWheelPaint.setStyle(Paint.Style.STROKE);// 空心
        mFinishWheelPaint.setStrokeCap(Paint.Cap.ROUND);// 圆角画笔
        mFinishWheelPaint.setAntiAlias(true);// 去锯齿

        mDefaultWheelPaint = new Paint();
        mDefaultWheelPaint.setColor(Color.argb(1, 241, 241, 241));
        mDefaultWheelPaint.setStyle(Paint.Style.STROKE);
        mDefaultWheelPaint.setStrokeCap(Paint.Cap.ROUND);
        mDefaultWheelPaint.setAntiAlias(true);

        mBitmapPaint = new Paint();
        mBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_hi_timer)).getBitmap();

        mAnim = new BarAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mWheelRect, 0, 359, false, mDefaultWheelPaint);
        canvas.drawArc(mWheelRect, 0, mSweepAnglePer, false, mFinishWheelPaint);
        SweepGradient sweepGradient = new SweepGradient(mWheelRect.centerX(), mWheelRect.centerY(),
                new int[]{getResources().getColor(R.color.colorGradientOrange),
                        getResources().getColor(R.color.colorGradientRed)}, null);
        mFinishWheelPaint.setShader(sweepGradient);

        canvas.drawBitmap(mBitmap, (float) (mBitmapRect.centerX() - mBitmap.getWidth() * 3F / 4 + mBitmapRect.width() / 2F * Math.sin(mSweepAnglePer)), (float) (mBitmapRect.centerY() - mBitmap.getWidth() * 3F / 4 - mBitmapRect.width() / 2F * Math.cos(mSweepAnglePer)), mBitmapPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形
        mCircleStrokeWidth = getTextScale(15, min);// 圆弧的宽度
        pressExtraStrokeWidth = getTextScale(10, min);// 圆弧离矩形的距离
        mWheelRect.set(mCircleStrokeWidth, mCircleStrokeWidth, min - mCircleStrokeWidth, min - mCircleStrokeWidth);// 设置矩形
        mBitmapRect.set(mCircleStrokeWidth + mBitmap.getWidth() / 2, mCircleStrokeWidth + mBitmap.getWidth() / 2, min - mBitmap.getWidth() / 2, min - mBitmap.getWidth() / 2);// 设置矩形
        mFinishWheelPaint.setStrokeWidth(mCircleStrokeWidth);
        mDefaultWheelPaint.setStrokeWidth(mCircleStrokeWidth - getTextScale(2, min));
        mDefaultWheelPaint.setShadowLayer(getTextScale(10, min), 0, 0, Color.rgb(127, 127, 127));// 设置阴影
    }

    /**
     * 进度条动画
     *
     * @author Administrator
     */
    public class BarAnimation extends Animation {

        /**
         * 每次系统调用这个方法时， 改变mSweepAnglePer，mPercent，stepnumbernow的值，
         * 然后调用postInvalidate()不停的绘制view。
         */
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mPercent = Float.parseFloat(mDecimalFormat.format(mStepNum * 100f / mMaxStepNum));// 将浮点值四舍五入保留一位小数
            if (mPercent > 100.0f) {
                mPercent = 100.0f;
            }
            PERCENT = String.valueOf(mPercent);
            mSweepAnglePer = mStepNum * 360 / mMaxStepNum;
            mCurrStepNum = mStepNum;
//			}

            requestLayout();
        }
    }

    public float getPercent() {
        return mPercent;
    }

    /**
     * 根据控件的大小改变绝对位置的比例
     *
     * @param n
     * @param m
     * @return
     */
    public float getTextScale(float n, float m) {
        return n / 500 * m;
    }

    /**
     * 更新步数和设置一圈动画时间
     *
     * @param stepCount
     * @param time
     */
    public void update(long stepCount, int time) {
        this.mStepNum = stepCount;
        mAnim.setDuration(time);
        // setAnimationTime(time);
        this.startAnimation(mAnim);
    }

    /**
     * @param stepNum
     */
    public void setMaxStepNum(int stepNum) {
        mMaxStepNum = stepNum;
        GOAL_STEP = String.valueOf(mMaxStepNum);
    }

    public void setColor(int color) {
        mFinishWheelPaint.setColor(color);
    }

    /**
     * 设置动画时间
     *
     * @param time
     */
    public void setAnimationTime(int time) {
        mAnim.setDuration(time * mStepNum / mMaxStepNum);// 按照比例设置动画执行时间

    }

}
