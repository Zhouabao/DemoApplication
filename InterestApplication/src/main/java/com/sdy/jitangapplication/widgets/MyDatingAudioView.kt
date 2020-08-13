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
import com.sdy.jitangapplication.utils.UriUtils
import kotlinx.android.synthetic.main.layout_dating_audio.view.*

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
        audioTip: String = "点击试听约会语音描述"
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
    private var duration: Int = 0
    private var playState: Int = MEDIA_PREPARE

    private fun initAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        mediaPlayer = IjkMediaPlayerUtil(context, 0, object : OnPlayingListener {
            override fun onPlay(position: Int) {
                audioPlayBtn.setImageResource(pauseIcon)
            }

            override fun onPause(position: Int) {
                audioPlayBtn.setImageResource(playIcon)
            }

            override fun onStop(position: Int) {
                audioPlayBtn.setImageResource(playIcon)

            }

            override fun onError(position: Int) {
                CommonFunction.toast("音频播放出错")
                audioPlayBtn.setImageResource(playIcon)
                mediaPlayer!!.resetMedia()
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                audioPlayBtn.setImageResource(playIcon)
            }

            override fun onRelease(position: Int) {

//                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
                audioPlayBtn.setImageResource(playIcon)
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

        }).getInstance()

        audioPlayBtn.clickWithTrigger {
            if (playState == MEDIA_PREPARE || playState == MEDIA_PAUSE || playState == MEDIA_STOP || playState == MEDIA_ERROR) {
                playState = MEDIA_PLAY
            } else if (playState == MEDIA_PLAY) {
                playState = MEDIA_PAUSE
            }

            when (playState) {
                MEDIA_PREPARE, MEDIA_ERROR, MEDIA_STOP -> {
                    playAudio()
                }
                MEDIA_PAUSE -> {
                    resumeAudio()
                }
                MEDIA_PLAY -> {
                    pauseAudio()
                }

            }
        }
    }

    fun playAudio() {
        audioTime.startTime(duration.toLong(), "3")
        mediaPlayer!!.prepareMedia()
    }

    fun prepareAudio(path: String, duration: Int) {
        filePath = path
        this.duration = duration
        initAudio()
        mediaPlayer!!.setDataSource(path)
        audioTime.text = UriUtils.getShowTime(duration)
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


    fun resumeAudio() {
        if (mediaPlayer != null)
            mediaPlayer!!.resumePlay()
    }
}