package com.sdy.baselibrary.utils

import android.view.View

abstract class CustomClickListener(private val timeInterval:Long = 1500L) : View.OnClickListener {
    private var mLastClickTime: Long = 0

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > timeInterval) {
            // 单次点击事件
            onSingleClick(v)
            mLastClickTime = nowTime
        }
    }

    protected abstract fun onSingleClick(view: View)
}