package com.example.demoapplication.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author : GuZhC
 * @date :  2019/6/26 20:09
 * @description : CardViewPager2
 */
public class CardViewPager2 extends ViewPager2 {
    public CardViewPager2(@NonNull Context context) {
        this(context, null);
    }

    public CardViewPager2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CardViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    float mDownX = 0;
    float mDownY = 0;
//    @Override
//    public boolean dispatchTouchEvent (MotionEvent ev) {
//        int action = ev.getAction();
//        if (action == MotionEvent.ACTION_DOWN) {
//            mDownX = ev.getX();
//            mDownY = ev.getY();
//            return true;
//        } else if (action == MotionEvent.ACTION_MOVE) {
//            float moveX = ev.getX();
//            float moveY = ev.getY();
//            float diffX = moveX - mDownX;
//            float diffY = moveY - mDownY;
//            // 如果是水平操作，才考虑要不要拦截
//            if (Math.abs(diffX) > Math.abs(diffY)) {
//                return false;
//            } else {
//                return true;
//            }
//        } else {
//            return true;
//        }
//    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mDownX = ev.getX();
//                mDownY = ev.getY();
//                getParent().requestDisallowInterceptTouchEvent(true);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float moveX = ev.getX();
//                float moveY = ev.getY();
//                float diffX = moveX - mDownX;
//                float diffY = moveY - mDownY;
//                // 如果是水平操作，才考虑要不要拦截
//                if (Math.abs(diffX) > Math.abs(diffY)) {
//                    getParent().requestDisallowInterceptTouchEvent(false);
//                } else {//其他情况，由孩子拦截触摸事件
//                    getParent().requestDisallowInterceptTouchEvent(true);
//                }
//            default:
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return false;
//    }
}
