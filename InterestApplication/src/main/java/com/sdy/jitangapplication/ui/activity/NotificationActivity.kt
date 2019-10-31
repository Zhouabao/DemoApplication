package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.widget.CompoundButton
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.netease.nimlib.sdk.NIMClient
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.nim.DemoCache
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


        switchDianzan.isChecked = SPUtils.getInstance(Constants.SPNAME).getBoolean("switchDianzan", true)
        switchComment.isChecked = SPUtils.getInstance(Constants.SPNAME).getBoolean("switchComment", true)
        switchMusic.isChecked = DemoCache.getNotificationConfig().ring
        switchVibrator.isChecked = DemoCache.getNotificationConfig().vibrate
    }


    override fun onCheckedChanged(button: CompoundButton, check: Boolean) {
        when (button.id) {
            //点赞提醒
            R.id.switchDianzan -> {
                SPUtils.getInstance(Constants.SPNAME).put("switchDianzan", check)
            }
            //评论提醒
            R.id.switchComment -> {
                SPUtils.getInstance(Constants.SPNAME).put("switchComment", check)
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

            }
        }
    }

}
