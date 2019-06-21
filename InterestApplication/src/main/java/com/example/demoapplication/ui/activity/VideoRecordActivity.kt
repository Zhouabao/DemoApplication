package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.demoapplication.R
import com.example.demoapplication.videorecord.ComposeRecordBtn
import kotlinx.android.synthetic.main.activity_video_record.*

/**
 * 视频录制activity
 */
class VideoRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record)

        compose_record_btn.recordMode = ComposeRecordBtn.RECORD_MODE_TAKE_PHOTO
    }
}
