package com.example.demoapplication.switchplay

import android.view.View


object SwitchUtil {
    public var sSwitchVideo: SwitchVideo? = null

    fun optionPlayer(gsyVideoPlayer: SwitchVideo, url: String, cache: Boolean, title: String) {
        //增加title
        gsyVideoPlayer.titleTextView.visibility = View.GONE
        //设置返回键
        gsyVideoPlayer.backButton.visibility = View.GONE
        //设置全屏按键功能
        gsyVideoPlayer.fullscreenButton.setOnClickListener {
            gsyVideoPlayer.startWindowFullscreen(
                gsyVideoPlayer.context,
                false,
                false
            )
        }
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
        gsyVideoPlayer.isAutoFullWithSize = true
        //音频焦点冲突时是否释放
        gsyVideoPlayer.isReleaseWhenLossAudio = false
        //全屏动画
        gsyVideoPlayer.isShowFullAnimation = true
        //小屏时不触摸滑动
        gsyVideoPlayer.setIsTouchWiget(false)

        gsyVideoPlayer.setSwitchUrl(url)

        gsyVideoPlayer.setSwitchCache(cache)

        gsyVideoPlayer.setSwitchTitle(title)
    }


    fun savePlayState(switchVideo: SwitchVideo) {
        sSwitchVideo = switchVideo.saveState()
    }

    fun clonePlayState(switchVideo: SwitchVideo) {
        if (sSwitchVideo != null)
            switchVideo.cloneState(sSwitchVideo!!)
    }

    fun release() {
        sSwitchVideo = null
    }
}
