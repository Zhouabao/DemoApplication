package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.presenter.NotificationPresenter
import com.sdy.jitangapplication.presenter.view.NotificationView
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 通知提醒
 */
class NotificationActivity : BaseMvpActivity<NotificationPresenter>(), NotificationView,
    CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initView()
    }

    private fun initView() {
        mPresenter = NotificationPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        btnBack.onClick {
            finish()
        }
        hotT1.text = "消息提醒"

        tvSwitchComment.setOnClickListener(this)
        tvSwitchDianzan.setOnClickListener(this)
        //switchDianzan.setOnCheckedChangeListener(this)
        //switchComment.setOnCheckedChangeListener(this)
        switchReply.setOnCheckedChangeListener(this)
        switchMusic.setOnCheckedChangeListener(this)
        switchVibrator.setOnCheckedChangeListener(this)

        //// notify_square_like_state  notify_square_comment_state

        switchDianzan.isChecked = intent.getBooleanExtra("notify_square_like_state", true)
        switchComment.isChecked = intent.getBooleanExtra("notify_square_comment_state", true)
        switchMusic.isChecked = DemoCache.getNotificationConfig().ring
        switchVibrator.isChecked = DemoCache.getNotificationConfig().vibrate
    }


    override fun onCheckedChanged(button: CompoundButton, check: Boolean) {
        when (button.id) {


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


    override fun onClick(view: View) {
        when (view) {
            tvSwitchDianzan -> {    //点赞提醒
                mPresenter.squareNotifySwitch(1)
            }
            tvSwitchComment -> {     //评论提醒
                mPresenter.squareNotifySwitch(2)
            }
        }
    }


    //用户广场点赞/评论接收推送开关 参数 type（int）型    1点赞    2评论
    override fun onGreetApproveResult(type: Int, success: Boolean) {

        when (type) {
            1 -> {
                switchDianzan.isChecked = !switchDianzan.isChecked
            }
            2 -> {
                switchComment.isChecked = !switchComment.isChecked

            }
        }
    }
}
