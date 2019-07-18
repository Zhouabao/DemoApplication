package com.example.demoapplication.player

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 *    author : ZFM
 *    date   : 2019/7/189:31
 *    desc   :
 *    version: 1.0
 */
class MusicService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}