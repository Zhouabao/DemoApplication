package com.example.demoapplication.common

import android.content.Context
import com.blankj.utilcode.util.NetworkUtils
import com.example.demoapplication.R

/**
 *    author : ZFM
 *    date   : 2019/7/2214:52
 *    desc   :
 *    version: 1.0
 */
object CommonFunction {

    fun getErrorMsg(context: Context): String {
        return if (NetworkUtils.isConnected()) {
            context.getString(R.string.retry_load_error)
        } else {
            context.getString(R.string.retry_net_error)

        }
    }
}