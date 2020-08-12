package com.sdy.jitangapplication.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
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

    private fun initAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        mediaPlayer = IjkMediaPlayerUtil(context, 0, object : OnPlayingListener {
            override fun onPlay(position: Int) {
//                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
//                voicePlayView.playAnimation()
//                UpdateVoiceTimeThread.getInstance(
//                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
//                    audioTime
//                ).start()
                audioPlayBtn.setImageResource(pauseIcon)
            }

            override fun onPause(position: Int) {
//                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
//                voicePlayView.cancelAnimation()
//                UpdateVoiceTimeThread.getInstance(
//                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
//                    audioTime
//                ).pause()
                audioPlayBtn.setImageResource(playIcon)
            }

            override fun onStop(position: Int) {
//                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
//                voicePlayView.cancelAnimation()
//                UpdateVoiceTimeThread.getInstance(
//                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
//                    audioTime
//                ).stop()
                audioPlayBtn.setImageResource(playIcon)

            }

            override fun onError(position: Int) {
                CommonFunction.toast("音频播放出错")
//                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
//                voicePlayView.cancelAnimation()
//                UpdateVoiceTimeThread.getInstance(
//                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
//                    audioTime
//                ).stop()
                audioPlayBtn.setImageResource(playIcon)
                mediaPlayer!!.resetMedia()
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
//                voicePlayView.cancelAnimation()
//                UpdateVoiceTimeThread.getInstance(
//                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
//                    audioTime
//                ).stop()
                audioPlayBtn.setImageResource(playIcon)
            }

            override fun onRelease(position: Int) {
//                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
//                voicePlayView.stop()
//                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
//                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
//                mediaPlayer!!.resetMedia()
//                mediaPlayer = null
            }

        }).getInstance()



        audioPlayBtn.setOnClickListener {
//            when (squareBean!!.isPlayAudio) {
//                IjkMediaPlayerUtil.MEDIA_ERROR -> {
//                    initAudio(0)
//                    mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "")
//                        .prepareMedia()
//                }
//                IjkMediaPlayerUtil.MEDIA_PREPARE -> {//准备中
//                    mediaPlayer!!.prepareMedia()
//                }
//                IjkMediaPlayerUtil.MEDIA_STOP -> {//停止就重新准备
//                    initAudio(0)
//                    mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "")
//                        .prepareMedia()
//                }
//                IjkMediaPlayerUtil.MEDIA_PLAY -> {//播放点击就暂停
//                    mediaPlayer!!.pausePlay()
//                }
//                IjkMediaPlayerUtil.MEDIA_PAUSE -> {//暂停再次点击就播放
//                    mediaPlayer!!.resumePlay()
//                }
//            }
        }
    }

    fun playAudio() {
        mediaPlayer!!.startPlay()
    }

    fun prepareAudio() {
//        mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "").prepareMedia()
        initAudio()
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