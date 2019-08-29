package com.example.demoapplication.switchplay


object SwitchUtil {
    public var sSwitchVideo: SwitchVideo? = null

    fun optionPlayer(gsyVideoPlayer: SwitchVideo, url: String, cache: Boolean) {
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
        gsyVideoPlayer.isAutoFullWithSize = false
        //音频焦点冲突时是否释放
        gsyVideoPlayer.isReleaseWhenLossAudio = false
        //全屏动画
        gsyVideoPlayer.isShowFullAnimation = true
        //小屏时不触摸滑动
        gsyVideoPlayer.setIsTouchWiget(false)

        gsyVideoPlayer.isShowPauseCover = true

        gsyVideoPlayer.dismissControlTime = 500
        //是否循环播放
//        gsyVideoPlayer.isLooping = true

        gsyVideoPlayer.setSwitchUrl(url)

        gsyVideoPlayer.setSwitchCache(cache)

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
