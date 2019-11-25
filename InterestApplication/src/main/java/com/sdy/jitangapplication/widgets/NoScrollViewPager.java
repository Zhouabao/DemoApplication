package com.sdy.jitangapplication.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.Px;
import androidx.viewpager.widget.ViewPager;

/**
 * author : ZFM
 * date   : 2019/7/1818:02
 * desc   :
 * version: 1.0
 */
public class NoScrollViewPager extends ViewPager {

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        super.scrollTo(x, y);
    }

    //是否可以滑动
    private boolean isCanScroll = true;

    //----------禁止左右滑动------------------
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isCanScroll) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        } else {
            return false;
        }

    }
    //-------------------------------------------

    /**
     * 设置 是否可以滑动
     *
     * @param isCanScroll
     */
    public void setScrollable(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }
}
