package com.sdy.jitangapplication.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 *    author : ZFM
 *    date   : 2019/9/259:16
 *    desc   :
 *    version: 1.0
 */
class MyViewPager @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null) :
    ViewPager(context, attributes) {
    override fun onTouchEvent(ev: MotionEvent?): Boolean {

        try {
            return super.onTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}