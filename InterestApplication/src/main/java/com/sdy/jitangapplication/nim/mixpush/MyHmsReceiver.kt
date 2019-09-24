package com.sdy.jitangapplication.nim.mixpush

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.netease.nimlib.sdk.mixpush.HWPushMessageReceiver

/**
 *    author : ZFM
 *    date   : 2019/9/2410:08
 *    desc   :
 *    version: 1.0
 */
class MyHmsReceiver : HWPushMessageReceiver() {


    override fun onToken(context: Context?, token: String?, extras: Bundle?) {

        Log.d("OkHttp","======$token=========")
    }

}