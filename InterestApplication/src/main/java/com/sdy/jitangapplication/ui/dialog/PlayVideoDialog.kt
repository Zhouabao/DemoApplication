package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.dialog_play_video.*

/**
 *    author : ZFM
 *    date   : 2020/5/2616:24
 *    desc   : 解锁播放视频
 *    version: 1.0
 */
class PlayVideoDialog(val context1: Context, var mv_url: String) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_play_video)
        initWindow()
        showVideoPreview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT

        window?.attributes = params

        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    private fun showVideoPreview() {
        CommonFunction.initVideo(context1, videoPreview, mv_url)
//        videoPreview.setMediaController(MediaController(this))
//        videoPreview.setVideoPath(mv_url)
//        videoPreview.setOnPreparedListener {
//            coverBlack.isVisible = false
//        }
//        videoPreview.setOnCompletionListener {
//            videoPreview.start()
//        }

//        playVideo.onClick {
//            videoPreview.start()
//            coverBlack.isVisible = false
//            playVideo.isVisible = false
//        }

        videoPreview.backButton.onClick {
            dismiss()
        }
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
        GSYVideoManager.releaseAllVideos()

    }

}