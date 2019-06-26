package com.example.demoapplication.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @author : GuZhC
 * @date :  2019/6/26 20:47
 * @description : CardConstraintLayout
 */
public class CardConstraintLayout extends ConstraintLayout {
    public CardConstraintLayout(Context context) {
        this(context,null);
    }

    public CardConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CardConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float mDownPosX = 0;
    private float mDownPosY = 0;

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        final float x = ev.getX();
//        final float y = ev.getY();
//        final int action = ev.getAction();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mDownPosX = x;
//                mDownPosY = y;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final float deltaX = Math.abs(x - mDownPosX);
//                final float deltaY = Math.abs(y - mDownPosY);
//                // 这里是否拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截
//                if (deltaX > deltaY) {// 左右滑动拦截
//                    return true;
//                }
//        }
//        return false;
//    }
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        final float x = ev.getX();
//        final float y = ev.getY();
//        final int action = ev.getAction() ;
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mDownPosX = ev.getRawX();
//                mDownPosY = ev.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final float deltaX = Math.abs(x - mDownPosX);
//                final float deltaY = Math.abs(y - mDownPosY);
//                if (deltaX > deltaY) {// 左右滑动拦截
//                    return true;
//                } else {
//                    return super.onTouchEvent(ev);
//                }
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        return false;
//    }
}

