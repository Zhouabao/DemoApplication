package com.sdy.jitangapplication.common

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.ui.dialog.SayHiDialog
import com.sdy.jitangapplication.utils.UserManager

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
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.showShort(msg)
    }


    /**
     * 打招呼通用逻辑
     */
    fun commonGreet(
        context: Context,
        isfriend: Boolean,
        greet_switch: Boolean,//接收招呼开关   true  接收招呼      false   不接受招呼
        greet_state: Boolean,// 认证招呼开关   true  开启认证      flase   不开启认证
        targetAccid: String,
        targetNickName: String,
        isgreeted: Boolean = false,//招呼是否有效
        view: View
    ) {
        view.isEnabled = false
        if (isfriend || isgreeted) {
            ChatActivity.start(context, targetAccid)
        } else {
            if (UserManager.getLightingCount() > 0) {
                if (!greet_switch) {
                    toast("对方已关闭招呼功能")
                } else {
                    if (greet_state && UserManager.isUserVerify() != 1) {
                        HarassmentDialog(context, HarassmentDialog.CHATHI).show()
                    } else {
                        SayHiDialog(targetAccid, targetNickName, context).show()
                    }
                }
            } else {
                ChargeVipDialog(ChargeVipDialog.DOUBLE_HI, context, ChargeVipDialog.PURCHASE_GREET_COUNT).show()
            }
        }
        view.postDelayed({ view.isEnabled = true }, 500L)
    }
}