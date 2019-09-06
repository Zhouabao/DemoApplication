package com.sdy.jitangapplication.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.blankj.utilcode.util.SizeUtils;
import com.sdy.jitangapplication.R;

/**
 * author : ZFM
 * date   : 2019/7/2614:47
 * desc   : 圆形进度条带渐变色
 * version: 1.0
 */
public class MsgTimerProgress extends View {
    //绘制背景圆弧
    private Paint mDefaultWheelPaint;
    private RectF mWheelRect = new RectF();
    //绘制圆弧
    private Paint mFinishWheelPaint;
    private float mCircleStrokeWidth;

    private float timerWidth;
    private float bitmapWidth;

    private float mSweepAnglePer;
    private long mStepNum;
    private int mMaxStepNum;// 默认最大步数


    private RectF mBitmapRect = new RectF();
    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    //圆心坐标，半径

    private Point point = new Point();
    private float mRadius;


    private BarAnimation mAnim;

    public MsgTimerProgress(Context context) {
        super(context);
        init(null, context);
    }

    public MsgTimerProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public MsgTimerProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, context);
    }

    private void init(AttributeSet attrs, Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MsgTimerProgress);
        timerWidth = typedArray.getInt(R.styleable.MsgTimerProgress_timerWidth, 81);
        bitmapWidth = typedArray.getInt(R.styleable.MsgTimerProgress_bitmapWidth, 16);

        mCircleStrokeWidth = SizeUtils.dp2px(2f);// 圆弧的宽度

        mFinishWheelPaint = new Paint();
        mFinishWheelPaint.setColor(Color.WHITE);
        mFinishWheelPaint.setStyle(Paint.Style.STROKE);// 空心
        mFinishWheelPaint.setStrokeCap(Paint.Cap.BUTT);// 圆角画笔
        mFinishWheelPaint.setAntiAlias(true);// 去锯齿
        mFinishWheelPaint.setStrokeWidth(mCircleStrokeWidth);


        mDefaultWheelPaint = new Paint();
        mDefaultWheelPaint.setColor(Color.rgb(255, 0, 62));
        mDefaultWheelPaint.setStyle(Paint.Style.STROKE);
        mDefaultWheelPaint.setStrokeCap(Paint.Cap.ROUND);
        mDefaultWheelPaint.setStrokeWidth(mCircleStrokeWidth);
        mDefaultWheelPaint.setAntiAlias(true);


        mBitmapPaint = new Paint();
        mBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_hi_timer)).getBitmap();
        mAnim = new BarAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(270, point.x, point.y);
        SweepGradient sweepGradient = new SweepGradient(point.x, point.y,
                new int[]{getResources().getColor(R.color.colorGradientOrange),
                        getResources().getColor(R.color.colorGradientRed)}, null);
        mDefaultWheelPaint.setShader(sweepGradient);
        canvas.drawArc(mWheelRect, 0, 360, false, mDefaultWheelPaint);
        canvas.drawArc(mWheelRect, 0, mSweepAnglePer, false, mFinishWheelPaint);
        canvas.translate(point.x, point.y);
        canvas.rotate(mSweepAnglePer);

        Bitmap bitmap = imageScale(mBitmap, bitmapWidth, bitmapWidth);
        canvas.drawBitmap(bitmap, mRadius - bitmap.getWidth() / 2, -bitmap.getHeight() / 2, mDefaultWheelPaint);
        canvas.translate(mRadius - bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
//        int timerWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = SizeUtils.dp2px(this.timerWidth);
        int width = SizeUtils.dp2px(this.timerWidth);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minsize = (int) (w - 2 * mCircleStrokeWidth - SizeUtils.dp2px(bitmapWidth));
        mRadius = minsize / 2;
        point.x = w / 2;
        point.y = h / 2;

        mWheelRect.left = point.x - mRadius - mCircleStrokeWidth / 2;
        mWheelRect.top = point.y - mRadius - mCircleStrokeWidth / 2;
        mWheelRect.right = point.x + mRadius + mCircleStrokeWidth / 2;
        mWheelRect.bottom = point.y + mRadius + mCircleStrokeWidth / 2;
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
            mSweepAnglePer = mStepNum * 360 / mMaxStepNum;
            requestLayout();
        }
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


    /**
     * 调整图片大小
     *
     * @param bitmap 源
     * @param dst_w  输出宽度
     * @param dst_h  输出高度
     * @return
     */
    public static Bitmap imageScale(Bitmap bitmap, float dst_w, float dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) SizeUtils.dp2px(dst_w)) / src_w;
        float scale_h = ((float) SizeUtils.dp2px(dst_h)) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }
}
