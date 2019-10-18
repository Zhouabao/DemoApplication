package com.sdy.jitangapplication.common

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.sdy.jitangapplication.R

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


    fun toast(msg: String) {
        ToastUtils.setBgColor(Color.parseColor("#80000000"))
        ToastUtils.setMsgColor(Color.WHITE)
        ToastUtils.setGravity(Gravity.CENTER,0,0)
        ToastUtils.showShort(msg)
    }
}