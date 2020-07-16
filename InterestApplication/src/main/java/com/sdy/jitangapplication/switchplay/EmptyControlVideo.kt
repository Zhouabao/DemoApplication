package com.sdy.jitangapplication.switchplay

import android.content.Context
import android.util.AttributeSet
import com.sdy.jitangapplication.R
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

/**
 *    author : ZFM
 *    date   : 2020/7/1516:42
 *    desc   :
 *    version: 1.0
 */
class EmptyControlVideo : StandardGSYVideoPlayer {

    constructor(context: Context, fullFlag: Boolean?) : super(context, fullFlag!!) {}

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun getLayoutId(): Int {
        return R.layout.empty_control_video
    }

    override fun touchSurfaceMoveFullLogic(absDeltaX: Float, absDeltaY: Float) {
        mChangePosition = false

        mChangeVolume = false

        mBrightness = false
    }


}