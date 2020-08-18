package com.sdy.jitangapplication.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.player.UpdateVoiceTimeThread
import com.sdy.jitangapplication.utils.UriUtils
import kotlinx.android.synthetic.main.layout_dating_audio.view.*
import kotlinx.android.synthetic.main.layout_record_audio.*

/**
 *    author : ZFM
 *    date   : 2020/8/1210:23
 *    desc   :
 *    version: 1.0
 */
class MyDatingAudioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val MEDIA_PREPARE = 0
        const val MEDIA_PLAY = 1
        const val MEDIA_PAUSE = 2
        const val MEDIA_STOP = 3
        const val MEDIA_ERROR = 4
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_dating_audio, this)
    }


    fun setUi(
        bgResource: Int = R.drawable.gradient_rectangle_orange_22dp,
        textColor: Int = Color.WHITE,
        playIcon: Int = R.drawable.icon_play_dating_audio,
        pauseIcon: Int = R.drawable.icon_pause_dating_audio,
        audioTip: String = "点击试听活动语音描述"
    ) {
        audioRecordLl.setBackgroundResource(bgResource)
        audioTime.setTextColor(textColor)
        audioPlayTip.setTextColor(textColor)
        audioPlayTip.text = audioTip
        this.playIcon = playIcon
        this.pauseIcon = pauseIcon
        audioPlayBtn.setImageResource(playIcon)
    }

    var playIcon = -1
    var pauseIcon = -1
    var mediaPlayer: IjkMediaPlayerUtil? = null
    private var filePath = ""
    var duration: Int = 0
    private var playState: Int = MEDIA_PREPARE

    private fun initAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        mediaPlayer = IjkMediaPlayerUtil(context, 0, object : OnPlayingListener {
            override fun onPlay(position: Int) {
                audioPlayBtn.setImageResource(pauseIcon)
                UpdateVoiceTimeThread.getInstance(
                    UriUtils.getShowTime(duration) ,
                    audioTime
                ).start()
            }

            override fun onPause(position: Int) {
                audioPlayBtn.setImageResource(playIcon)
                UpdateVoiceTimeThread.getInstance(
                     UriUtils.getShowTime(duration) ,
                    audioTime
                ).pause()
//                audioTime.stopTime()
            }

            override fun onStop(position: Int) {
                audioPlayBtn.setImageResource(playIcon)
//                audioTime.text = UriUtils.getShowTime(duration)
                playState = MEDIA_STOP
                UpdateVoiceTimeThread.getInstance(
                    UriUtils.getShowTime(duration) ,
                    audioTime
                ).stop()
            }

            override fun onError(position: Int) {
                CommonFunction.toast("音频播放出错")
//                audioTime.text = UriUtils.getShowTime(duration)
                UpdateVoiceTimeThread.getInstance(
                    UriUtils.getShowTime(duration) ,
                    audioTime
                ).stop()
                audioPlayBtn.setImageResource(playIcon)
                mediaPlayer!!.resetMedia()
                playState = MEDIA_ERROR
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                audioPlayBtn.setImageResource(playIcon)
                UpdateVoiceTimeThread.getInstance(
                    UriUtils.getShowTime(duration) ,
                    audioTime
                ).stop()
            }

            override fun onRelease(position: Int) {
                audioPlayBtn.setImageResource(playIcon)
                UpdateVoiceTimeThread.getInstance(
                    UriUtils.getShowTime(duration) ,
                    audioTime
                ).stop()
//                audioTime.text = UriUtils.getShowTime(duration)
//                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
                audioPlayBtn.setImageResource(playIcon)
                playState = MEDIA_PREPARE
            }

        }).getInstance()
        mediaPlayer!!.setDataSource(filePath)

    }

    fun playAudio() {
        initAudio()
        audioTime.startTime(duration.toLong(), "3")
        mediaPlayer!!.prepareMedia()
    }

    private var playTime = 0
    fun prepareAudio(path: String, duration: Int) {
        filePath = path
        this.duration = duration

        audioTime.text = UriUtils.getShowTime(duration)
        audioPlayBtn.clickWithTrigger {
            when (playState) {
                MEDIA_PREPARE, MEDIA_ERROR, MEDIA_STOP -> {
                    playAudio()
                    playState = MEDIA_PLAY
                }

                MEDIA_PAUSE -> {
                    resumeAudio()
                    playState = MEDIA_PLAY
                }
                MEDIA_PLAY -> {
                    pauseAudio()
                    playState = MEDIA_PAUSE
                }
            }

//            if (playState == MEDIA_PREPARE || playState == MEDIA_PAUSE || playState == MEDIA_STOP || playState == MEDIA_ERROR) {
//                playState = MEDIA_PLAY
//            } else if (playState == MEDIA_PLAY) {
//                playState = MEDIA_PAUSE
//            }
        }

    }


    fun releaseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }

    fun pauseAudio() {
        if (mediaPlayer != null)
            mediaPlayer!!.pausePlay()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer != null && mediaPlayer!!.currentState == MEDIA_PLAY
    }

    fun resumeAudio() {
        if (mediaPlayer != null)
            mediaPlayer!!.resumePlay()
    }
}