package com.sdy.jitangapplication.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 *    author : ZFM
 *    date   : 2019/6/2614:15
 *    desc   : 子布局通知父布局不要拦截事件，通过requestDisallowInterceptTouchEvent方法干预事件分发过程
 *    version: 1.0
 */
class ChildRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {
    private var onBottomCallback: OnBottomCallback? = null

    fun setOnBottomCallback(bottomCallback: OnBottomCallback) {
        this.onBottomCallback = bottomCallback
    }

    override fun onScrolled(dx: Int, dy: Int) {
        onBottomCallback?.onScrollBottom(isSlideToBottom())
    }


    private fun isSlideToBottom(): Boolean {
        return this != null && this.computeVerticalScrollExtent() + this.computeVerticalScrollOffset() >= this.computeVerticalScrollRange()
    }


    interface OnBottomCallback {
        fun onScrollBottom(isBottom: Boolean)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //父层ViewGroup不要拦截点击事件
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

}