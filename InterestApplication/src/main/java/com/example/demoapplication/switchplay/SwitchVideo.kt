package com.example.demoapplication.switchplay

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import kotlinx.android.synthetic.main.switch_video.view.*


/**
 *    author : ZFM
 *    date   : 2019/7/1310:03
 *    desc   :
 *    version: 1.0
 */
class SwitchVideo : StandardGSYVideoPlayer {


    constructor(context: Context, fullFlag: Boolean?) : super(context, fullFlag!!) {}

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun init(context: Context) {
        super.init(context)
        if (mIfCurrentIsFullscreen) {
            detail_btn!!.visibility = View.GONE
        }
    }

    override fun getLayoutId(): Int {
        return com.example.demoapplication.R.layout.switch_video
    }


    fun setSwitchUrl(url: String) {
        mUrl = url
        mOriginUrl = url
    }

    fun setSwitchCache(cache: Boolean) {
        mCache = cache
    }

    fun setSwitchTitle(title: String) {
        mTitle = title
    }

    fun setSurfaceToPlay() {
        addTextureView()
        gsyVideoManager.setListener(this)
        checkoutState()
    }

    fun saveState(): SwitchVideo {
        val switchVideo = SwitchVideo(context)
        cloneParams(this, switchVideo)
        return switchVideo
    }

    fun cloneState(switchVideo: SwitchVideo) {
        cloneParams(switchVideo, this)
    }

}
