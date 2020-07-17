package com.sdy.jitangapplication.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 *    author : ZFM
 *    date   : 2019/6/2614:15
 *    desc   : 可以监听滑动距离的scrollview
 *    version: 1.0
 */
class ScrollBottomRecyclerView @JvmOverloads constructor(
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


}