package com.example.demoapplication.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author : GuZhC
 * @date :  2019/6/27 0:33
 * @description : CardRecuclerView
 */
public class CardRecuclerView extends RecyclerView {
    private boolean isNeedIntercept = true;

    public CardRecuclerView(@NonNull Context context) {
        this(context,null);
    }

    public CardRecuclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public CardRecuclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private float downX ;    //按下时 的X坐标
    private float downY ;    //按下时 的Y坐标
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        float x= e.getX();
        float y = e.getY();
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                //将按下时的坐标存储
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //获取到距离差
                float dx= x-downX;
                float dy = y-downY;
                //通过距离差判断方向
                int orientation = getOrientation(dx, dy);
                switch (orientation) {
                    //左右滑动交给ViewPager处理
                    case 'r':
                        setNeedIntercept(true);
                        break;
                    //左右滑动交给ViewPager处理
                    case 'l':
                        setNeedIntercept(false);
                        break;
                }
                return isNeedIntercept;
        }
        return super.onInterceptTouchEvent(e);
    }

    public void setNeedIntercept(boolean needIntercept) {
        isNeedIntercept = needIntercept;
    }

    private int getOrientation(float dx, float dy) {
        if (Math.abs(dx)>Math.abs(dy)){
            //X轴移动
            return dx>0?'r':'l';//右,icon_left
        }else{
            //Y轴移动
            return dy>0?'b':'t';//下//上
        }
    }

}
