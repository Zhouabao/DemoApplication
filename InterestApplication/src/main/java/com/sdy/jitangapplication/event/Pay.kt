package com.sdy.jitangapplication.event

import android.content.Intent

/**
 *    author : ZFM
 *    date   : 2020/11/2619:52
 *    desc   :
 *    version: 1.0
 */

class PayPalResultEvent(val requestCode: Int, val resultCode: Int, val data: Intent?)
