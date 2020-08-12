package com.sdy.jitangapplication.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sdy.jitangapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * author : ZFM
 * date   : 2019/8/1317:36
 * desc   :水波纹外扩效果
 * version: 1.0
 */
public class SpreadView extends View {
    private boolean mRunning = false;

    private Paint centerPaint; // 中心圆paint
    private int radius = 30; // 中心圆半径
    private Paint spreadPaint; // 扩散圆paint
    private float centerX;// 圆心x
    private float centerY;// 圆心y
    private int distance = 5; // 每次圆递增间距
    private int maxRadius = 65; // 最大圆半径
    private int delayMilliseconds = 33;// 扩散延迟间隔，越大扩散越慢
    private List<Integer> spreadRadius = new ArrayList<>();// 扩散圆层级数，元素为扩散的距离
    private List<Integer> alphas = new ArrayList<>();// 对应每层圆的透明度

    public SpreadView(Context context) {
        this(context, null, 0);
    }

    public SpreadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private BitmapDrawable mBitmap;

    public SpreadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpreadView, defStyleAttr, 0);
        radius = a.getInt(R.styleable.SpreadView_spread_radius, radius);
        delayMilliseconds = a.getInt(R.styleable.SpreadView_spread_delay_milliseconds, 200);
        maxRadius = a.getInt(R.styleable.SpreadView_spread_max_radius, maxRadius);
        int centerColor = a.getColor(R.styleable.SpreadView_spread_center_color,
                ContextCompat.getColor(context, R.color.colorAccent));
        int spreadColor = a.getColor(R.styleable.SpreadView_spread_spread_color,
                ContextCompat.getColor(context, R.color.colorAccent));
        mRunning = a.getBoolean(R.styleable.SpreadView_spread_auto_running, false);
        Drawable drawable = a.getDrawable(R.styleable.SpreadView_spread_center_icon);
        mBitmap = (BitmapDrawable) drawable;
        distance = a.getInt(R.styleable.SpreadView_spread_distance, distance);
        a.recycle();

        centerPaint = new Paint();
        centerPaint.setColor(centerColor);
        centerPaint.setAntiAlias(true);
        initArray();
        spreadPaint = new Paint();
        spreadPaint.setAntiAlias(true);
        spreadPaint.setAlpha(255);
        spreadPaint.setColor(spreadColor);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 圆心位置
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 中间的圆
        // canvas.drawCircle(centerX, centerY, radius, centerPaint);
        // TODO 可以在中间圆绘制文字或者图片
        canvas.drawBitmap(mBitmap.getBitmap(), centerX - mBitmap.getBitmap().getWidth() / 2, centerX - mBitmap.getBitmap().getHeight() / 2, null);

        if (mRunning) {
            for (int i = 0; i < spreadRadius.size(); i++) {
                int alpha = alphas.get(i);
                spreadPaint.setAlpha(alpha);
                int width = spreadRadius.get(i);
                // 绘制扩散的圆
                canvas.drawCircle(centerX, centerY, radius + width, spreadPaint);

                // 每次扩散圆半径递增，圆透明度递减
                if (alpha > 0 && width < 130) {
                    alpha = alpha - distance > 0 ? alpha - distance : 1;
                    alphas.set(i, alpha);
                    spreadRadius.set(i, width + distance);
                }
            }
            // 当最外层扩散圆半径达到最大半径时添加新扩散圆
            if (spreadRadius.get(spreadRadius.size() - 1) > maxRadius) {
                spreadRadius.add(0);
                alphas.add(255);
            }
            // 超过8个扩散圆，删除最先绘制的圆，即最外层的圆
            if (spreadRadius.size() >= 5) {
                alphas.remove(0);
                spreadRadius.remove(0);
            }

            // 延迟更新，达到扩散视觉差效果
            postInvalidateDelayed(delayMilliseconds);
        }
    }


    public void start() {
        mRunning = true;
        postInvalidate();
    }

    public void stop() {
        mRunning = false;
        initArray();
        postInvalidate();
    }

    private void initArray() {
        // 最开始不透明且扩散距离为0
        alphas = new ArrayList<>();
        alphas.add(255);
        spreadRadius = new ArrayList<>();
        spreadRadius.add(0);
    }
}
