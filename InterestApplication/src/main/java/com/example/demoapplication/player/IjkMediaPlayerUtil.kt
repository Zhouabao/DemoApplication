package com.example.demoapplication.player

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 *    author : ZFM
 *    date   : 2019/7/189:36
 *    desc   :媒体播放器辅助类
 *    version: 1.0
 */
class IjkMediaPlayerUtil(val context: Context, val position: Int, val onPlayingListener: OnPlayingListener) :
    IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
    IMediaPlayer.OnPreparedListener {
    companion object {
        const val MEDIA_PREPARE = 0
        const val MEDIA_PLAY = 1
        const val MEDIA_PAUSE = 2
        const val MEDIA_STOP = 3
        const val MEDIA_ERROR = 4
    }

    private var pause = false
    private var mediaPlayer: IjkMediaPlayer? = null
    fun getInstance(): IjkMediaPlayerUtil {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
        mediaPlayer = IjkMediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        return this!!
    }


    public fun setDataSource(uri: String): IjkMediaPlayerUtil {
        mediaPlayer!!.setDataSource(context, Uri.parse(uri))
        return this!!
    }


    public fun prepareMedia(): IjkMediaPlayerUtil {
        mediaPlayer!!.prepareAsync()
        onPlayingListener.onPreparing(position)
        return this!!
    }


    public fun startPlay() {
        pause = false
        mediaPlayer!!.start()
        onPlayingListener.onPlay(position)
    }


    public fun pausePlay() {
        if (!pause && mediaPlayer!!.isPlaying) {
            pause = true
            mediaPlayer!!.pause()
        }
        onPlayingListener.onPause(position)
    }


    public fun resumePlay() {
        pause = false
        mediaPlayer!!.start()
        onPlayingListener.onPlay(position)
    }

    public fun resetMedia() {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        onPlayingListener.onRelease(position)

    }

    override fun onPrepared(mediaPlayer: IMediaPlayer?) {
        mediaPlayer!!.start()
        onPlayingListener.onPrepared(position)
    }

    override fun onCompletion(mediaPlayer: IMediaPlayer?) {
        if (mediaPlayer != null)
            mediaPlayer!!.release()
        onPlayingListener.onStop(position)
    }

    override fun onError(mediaPlayer: IMediaPlayer?, p1: Int, p2: Int): Boolean {
        if (mediaPlayer != null) {
            mediaPlayer.release()
        }
        onPlayingListener.onError(position)
        return false
    }
}