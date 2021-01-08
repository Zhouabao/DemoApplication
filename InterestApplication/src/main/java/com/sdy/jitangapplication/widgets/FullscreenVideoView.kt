package com.sdy.jitangapplication.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView
import com.blankj.utilcode.util.ScreenUtils

/**
 *    author : ZFM
 *    date   : 2019/6/2614:15
 *    desc   : 全屏的videoview
 *    version: 1.0
 */
class FullscreenVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    VideoView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())

    }
}