package com.example.demoapplication.player

import android.media.AudioManager
import android.media.MediaPlayer

/**
 *    author : ZFM
 *    date   : 2019/7/189:36
 *    desc   :
 *    version: 1.0
 */
class MediaPlayerUtil : MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener{


    private var pause = false
    private var mediaPlayer: MediaPlayer? = null
    fun getInstance(): MediaPlayerUtil {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        return this!!
    }


    public fun setDataSource(uri: String): MediaPlayerUtil {
        mediaPlayer!!.setDataSource(uri)
        return this!!
    }


    public fun prepareMedia(): MediaPlayerUtil {
        mediaPlayer!!.prepareAsync()
        return this!!
    }


    public fun startPlay() {
        mediaPlayer!!.start()
    }


    public fun pausePlay() {
        if (!pause && mediaPlayer!!.isPlaying) {
            pause = true
            mediaPlayer!!.pause()
        }
    }


    public fun resumePlay() {
        mediaPlayer!!.start()
    }

    public fun resetMedia() {
        if (mediaPlayer != null)
            mediaPlayer!!.release()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer!!.start()
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        if (mediaPlayer != null)
            mediaPlayer!!.release()
    }

    override fun onError(mediaPlayer: MediaPlayer?, p1: Int, p2: Int): Boolean {
        if (mediaPlayer != null) {
            mediaPlayer.release()
        }
        return false
    }

}