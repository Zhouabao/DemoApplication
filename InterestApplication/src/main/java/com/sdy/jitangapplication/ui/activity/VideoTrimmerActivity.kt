package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.VideoTrimmerEvent
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.videotrimmer.interfaces.VideoTrimListener
import kotlinx.android.synthetic.main.activity_video_trimmer.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult


/**
 * 视频片段截取
 */
class VideoTrimmerActivity : BaseActivity()/*, VideoTrimListener*/ {
    companion object {
        const val VIDEO_PATH_KEY = "video-file-path"
        const val VIDEO_TRIM_REQUEST_CODE = 0x001

        fun start(context: Context, videoPath: String) {
            if (!videoPath.isNullOrEmpty()) {
                (context as Activity).startActivityForResult<VideoTrimmerActivity>(
                    VIDEO_TRIM_REQUEST_CODE, VIDEO_PATH_KEY to videoPath
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trimmer)
//        initView()
    }

  /*  private fun initView() {
        hotT1.text = getString(R.string.choose_video)
        btnBack.clickWithTrigger { onCancel() }

        trimmer_view.setOnTrimVideoListener(this)
        trimmer_view.initVideoByURI(Uri.parse(intent.getStringExtra(VIDEO_PATH_KEY)))

    }

    override fun onFinishTrim(url: String) {
        EventBus.getDefault().post(VideoTrimmerEvent(url))
        finish()
    }

    override fun onStartTrim() {
    }

    override fun onCancel() {
        trimmer_view.onDestroy()
        finish()
    }

    override fun onPause() {
        super.onPause()
        trimmer_view.onVideoPause()
        trimmer_view.setRestoreState(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        trimmer_view.onDestroy()
    }*/
}