package com.sdy.jitangapplication.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.yuyakaido.android.cardstackview.CardStackView;

/**
 * author : ZFM
 * date   : 2020/4/3011:36
 * desc   :
 * version: 1.0
 */
public class HorizontalRecyclerView extends CardStackView {

    private float x1;
    private float y1;

    public HorizontalRecyclerView(Context context) {
        super(context);
    }

    public HorizontalRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);


        //解决recyclerView和viewPager的滑动影响
        //当滑动recyclerView时，告知父控件不要拦截事件，交给子view处理
//        get(false);
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                //当手指按下的时候
//                x1 = event.getX();
//                y1 = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                //当手指移动的时候
//                float x2 = event.getX();
//                float y2 = event.getY();
//                float offsetX = Math.abs(x2 - x1);
//                float offsetY = Math.abs(y2 - y1);
//                if (offsetX >= offsetY) {
//                    get(false);//手指左移
//                } else {
//                    get(true);
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                x1 = y1 = 0;
//                get(true);
//                break;
//        }
//        return super.dispatchTouchEvent(event);
    }

    private ViewParent mViewParent;

    //使用迭代 直至找到parent是NoScrollViewPager为止
    //效率有些低 偏low 莫见怪
    private void get(boolean isEnable) {
        if (mViewParent == null)
            mViewParent = getParent();
        else
            mViewParent = mViewParent.getParent();
        if (mViewParent instanceof NoScrollViewPager) {
            //true 禁止ViewPager滑动，自动交给recyclerview去滑动
            //false 交给ViewPager滑动
            NoScrollViewPager viewPager = (NoScrollViewPager) mViewParent;
            viewPager.setScrollable(isEnable);
        } else {
            get(isEnable);
        }
    }
}