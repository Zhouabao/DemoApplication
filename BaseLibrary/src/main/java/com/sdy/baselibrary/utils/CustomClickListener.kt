package com.sdy.baselibrary.utils

import android.view.View

abstract class CustomClickListener(private val timeInterval:Long = 1500L) : View.OnClickListener {
    private var mLastClickTime: Long = 0

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > timeInterval) {
            // 单次点击事件
            onSingleClick()
            mLastClickTime = nowTime
        } else {
            // 快速点击事件
            onFastClick()
        }
    }

    protected abstract fun onSingleClick()
    protected abstract fun onFastClick()
}