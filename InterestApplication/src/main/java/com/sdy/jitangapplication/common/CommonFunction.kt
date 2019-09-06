package com.sdy.jitangapplication.common

import android.content.Context
import com.blankj.utilcode.util.NetworkUtils
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


    /**
     *  点击聊天
     *  1. 好友 直接聊天 已经匹配过了
     *
     *  2. 不是好友 判断是否打过招呼
     *
     *     2.1 打过招呼 且没有过期  直接跳转界面
     *
     *     2.2 未打过招呼 判断招呼剩余次数
     *
     *         2.2.1 有次数 直接打招呼
     *
     *         2.2.2 无次数 其他操作--如:请求充值会员
     */
    fun sayHi(context: Context, targetAccid: String, hiCount: Int, isVip: Boolean) {

    }
}