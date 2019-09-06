package com.sdy.jitangapplication.widgets

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager2.widget.ViewPager2

/**
 *    author : ZFM
 *    date   : 2019/6/2411:13
 *    desc   : 竖直滑动的viewpager，主要用于匹配页面的
 *    version: 1.0
 */
class VerticalScrollViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewPager2(context, attrs, defStyleAttr) {
    private var downP = PointF()
    private var curP = PointF()
    private var xDown: Float = 0f
    private var yDown: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        curP.x = event.x
        curP.y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            downP.x = event.x
            downP.y = event.y

            xDown = event.x
            yDown = event.y

            parent.requestDisallowInterceptTouchEvent(true)
            if (event.action == MotionEvent.ACTION_MOVE) {
                val xMove = event.x
                val yMove = event.y

                //横向滑动，父控件进行事件拦截
                if (Math.abs(xMove - xDown) < Math.abs(yMove - yDown) && Math.abs(yMove - yDown) > 2) {
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    if (Math.abs(yMove - yDown) > 2) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        return false
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}