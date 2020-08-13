package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.activity_register_too_many.*
import org.jetbrains.anko.startActivity

/**
 * 排队等待注册中...
 */
class RegisterTooManyActivity : BaseActivity(), MediaPlayer.OnErrorListener {
    private val duration by lazy { intent.getIntExtra("duration", 0) }


    companion object {
        fun start(duration: Int, context: Context) {
            context.startActivity<RegisterTooManyActivity>("duration" to duration)
            (context as Activity).finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_too_many)
        initView()
        showVideoPreview()
    }

    private fun initView() {

        (btnBack.layoutParams as ConstraintLayout.LayoutParams).topMargin =
            BarUtils.getStatusBarHeight() + SizeUtils.dp2px(10F)
        BarUtils.setStatusBarLightMode(this, false)
        btnBack.clickWithTrigger { finish() }

        timeRunTextView.startTime(duration.toLong(), "2")

    }


    override fun onRestart() {
        super.onRestart()
        showVideoPreview()
    }

    override fun onResume() {
        super.onResume()
        if (!videoPreview.isPlaying)
            videoPreview.start()
//        videoPreview.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPreview.stopPlayback()
    }


    private fun showVideoPreview() {
//        videoPreview.setMediaController(MediaController(this))
        videoPreview.setVideoURI(Uri.parse("android.resource://com.sdy.jitangapplication/${R.raw.login_video}"))
        videoPreview.setOnCompletionListener {
            videoPreview.start()
        }
        videoPreview.setOnErrorListener(this)
        videoPreview.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }


}