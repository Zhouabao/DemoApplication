package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.widget.CompoundButton
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.netease.nimlib.sdk.NIMClient
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.nim.DemoCache
import com.umeng.message.MsgConstant
import com.umeng.message.PushAgent
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 通知提醒
 */
class NotificationActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        hotT1.text = "消息提醒"

        switchDianzan.setOnCheckedChangeListener(this)
        switchComment.setOnCheckedChangeListener(this)
        switchReply.setOnCheckedChangeListener(this)
        switchMusic.setOnCheckedChangeListener(this)
        switchVibrator.setOnCheckedChangeListener(this)
    }


    override fun onCheckedChanged(button: CompoundButton, check: Boolean) {
        when (button.id) {
            //点赞提醒
            R.id.switchDianzan -> {
//                NIMClient.getService(MixPushService::class.java).enable(true)

            }
            //评论提醒
            R.id.switchComment -> {
            }
            //回复提醒
            R.id.switchReply -> {
                NIMClient.toggleNotification(check)
            }
            //通知音效
            R.id.switchMusic -> {
                val config = DemoCache.getNotificationConfig()
                config.ring = check
                NIMClient.updateStatusBarNotificationConfig(config)
            }
            //震动开关
            R.id.switchVibrator -> {
                val config = DemoCache.getNotificationConfig()
                config.vibrate = check
                NIMClient.updateStatusBarNotificationConfig(config)

                if (check) {
                    PushAgent.getInstance(this).notificationPlayVibrate = MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE
                } else {
                    PushAgent.getInstance(this).notificationPlayVibrate = MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE
                }
            }
        }
    }

}
