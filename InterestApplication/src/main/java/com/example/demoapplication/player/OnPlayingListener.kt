package com.example.demoapplication.player

/**
 *    author : ZFM
 *    date   : 2019/7/1810:15
 *    desc   :
 *    version: 1.0
 */
interface OnPlayingListener {
    companion object {
        val PAUSE = 1
        val PLAYING = 2
        val COMPLETE = 3
    }

    fun onPlay(position:Int)

    fun onPause(position:Int)

    fun onStop(position:Int)

    fun onError(position:Int)

    fun onPreparing(position:Int)

    fun onPrepared(position:Int)


    fun onRelease(position:Int)


}