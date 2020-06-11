package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ScreenUtils
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sina.weibo.sdk.share.BaseActivity
import kotlinx.android.synthetic.main.activity_video_verify_confirm.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.textColor
import java.io.File

/**
 *视频介绍确认界面
 */
class VideoVerifyConfirmActivity : BaseActivity() {

    companion object {
        const val RESULT_CODE_CONFIRM_VIDEO = 1008
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_verify_confirm)
        initView()
        showVideoPreview()
    }

    private fun initView() {
        StatusBarUtil.immersive(this)
        llTitle.setBackgroundResource(R.color.colorTransparent)
        hotT1.text = "视频介绍"
        hotT1.textColor = Color.WHITE
        btnBack.setImageResource(R.drawable.icon_back_white)


        val params = confirmVideoFl.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (VideoVerifyActivity.RATIO * params.width).toInt()
        confirmVideoFl.layoutParams = params

        confirmVideo.setOnPreparedListener {
            it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        }


        btnBack.clickWithTrigger {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        retake.clickWithTrigger {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        confirm.clickWithTrigger {
            setResult(Activity.RESULT_OK)
            finish()
        }


    }


    private val videoSavePath by lazy { intent.getStringExtra("path") }
    private fun showVideoPreview() {
//        videoPreview.setMediaController(MediaController(this))
        confirmVideo.setVideoURI(Uri.fromFile(File(videoSavePath)))
        confirmVideo.setOnCompletionListener {
            confirmVideo.start()
            tvBalanceTime.stop()
            tvBalanceTime.base = SystemClock.elapsedRealtime()
            tvBalanceTime.start()
        }
        confirmVideo.setOnPreparedListener {
            confirmVideo.start()
            tvBalanceTime.base = SystemClock.elapsedRealtime()
            tvBalanceTime.start()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        confirmVideo.stopPlayback()
        tvBalanceTime.stop()

    }
}
