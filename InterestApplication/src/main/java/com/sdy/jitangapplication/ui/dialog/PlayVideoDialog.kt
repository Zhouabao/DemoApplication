package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
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
        mv_url =
            "https://vdept.bdstatic.com/67595575393448417844347145375548/6839465a7047534e/e3875eb46f6085411f6b58a5bebfd8ac9ccca895cf0de9b560ee582cbddee11244d422f831b367210ac9b74c7c67aca3.mp4?auth_key=1590489563-0-0-df02513277d3cc50809c84c29a7d46bf"
//        videoPreview.setMediaController(MediaController(this))
        videoPreview.setVideoURI(Uri.parse(mv_url))
        videoPreview.setOnPreparedListener {
            coverBlack.isVisible = false
        }
        videoPreview.setOnCompletionListener {
            videoPreview.start()
        }

        playVideo.onClick {
            videoPreview.start()
            playVideo.isVisible = false
        }

        closeBtn.onClick {
            dismiss()
        }
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
        videoPreview.stopPlayback()

    }

}