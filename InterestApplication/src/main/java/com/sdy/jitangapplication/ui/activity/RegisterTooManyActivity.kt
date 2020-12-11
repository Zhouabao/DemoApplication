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
        loginFlashLottie.playAnimation()
    }

    override fun onResume() {
        super.onResume()
        loginFlashLottie.resumeAnimation()
    }

    override fun onPause() {
        super.onPause()
        loginFlashLottie.pauseAnimation()
    }
    override fun onDestroy() {
        super.onDestroy()
        loginFlashLottie.cancelAnimation()
        timeRunTextView.stopTime()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }


}