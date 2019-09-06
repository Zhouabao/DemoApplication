package com.sdy.jitangapplication.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 *    author : ZFM
 *    date   : 2019/6/2614:15
 *    desc   : 可以监听滑动距离的scrollview
 *    version: 1.0
 */
class ObservableScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ScrollView(context, attrs, defStyleAttr) {
    private var onScrollChangedListener: OnScrollChangedListener? = null

    fun setOnScrollViewChangedListener(onScrollChangedListener: OnScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (onScrollChangedListener != null) {
            onScrollChangedListener!!.onScrollChanged(this, l, t, oldl, oldt)

        }
    }

    interface OnScrollChangedListener {
        fun onScrollChanged(scrollView: ObservableScrollView, x: Int, y: Int, oldX: Int, oldY: Int)
    }



}