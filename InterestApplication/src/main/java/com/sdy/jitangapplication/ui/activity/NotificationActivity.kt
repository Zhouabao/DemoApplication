package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.core.view.isVisible
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NotificationUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.UpdateSettingEvent
import com.sdy.jitangapplication.event.UpdateWechatSettingsEvent
import com.sdy.jitangapplication.model.SettingsBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.presenter.NotificationPresenter
import com.sdy.jitangapplication.presenter.view.NotificationView
import com.sdy.jitangapplication.ui.dialog.SaveQRCodeDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 通知提醒
 */
class NotificationActivity : BaseMvpActivity<NotificationPresenter>(), NotificationView,
    CompoundButton.OnCheckedChangeListener, OnLazyClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initView()
        mPresenter.mySettings()
    }

    //    private val wechat_qrcode by lazy { intent.getStringExtra("wechat_qrcode") }
    var wechatPublicState = false //微信公众号是否绑定状态
    var wechatState = false //微信推送开启状态
    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = NotificationPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        btnBack.onClick {
            finish()
        }
        hotT1.text = getString(R.string.notification_title)

        tvSwitchComment.setOnClickListener(this)
        tvSwitchDianzan.setOnClickListener(this)
        switchMessageBtn.setOnClickListener(this)
        openPushBtn.setOnClickListener(this)
        switchWechatBtn.setOnClickListener(this)
        wechatPublic.setOnClickListener(this)
        //switchDianzan.setOnCheckedChangeListener(this)
        //switchComment.setOnCheckedChangeListener(this)
        switchReply.setOnCheckedChangeListener(this)
        switchMusic.setOnCheckedChangeListener(this)
        switchVibrator.setOnCheckedChangeListener(this)


        //// notify_square_like_state  notify_square_comment_state


        switchMusic.isChecked = DemoCache.getNotificationConfig().ring
        switchVibrator.isChecked = DemoCache.getNotificationConfig().vibrate

        if (NotificationUtils.areNotificationsEnabled()) {
            openPushStatus.text = getString(R.string.has_open)
        } else {
            openPushStatus.text = getString(R.string.not_open)
        }
        if (UserManager.overseas) {
            wechatTv.isVisible = false
            switchWechat.isVisible = false
            switchWechatBtn.isVisible = false
            wechatPublicTv.isVisible = false
            wechatPublic.isVisible = false
        }

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


    override fun onLazyClick(view: View) {
        when (view) {
            tvSwitchDianzan -> {//点赞提醒
                mPresenter.squareNotifySwitch(1)
            }
            tvSwitchComment -> {//评论提醒
                mPresenter.squareNotifySwitch(2)
            }
            switchMessageBtn -> {//短信通知开关
                mPresenter.switchSet(
                    1, if (switchMessage.isChecked) {
                        2
                    } else {
                        1
                    }
                )

            }
            openPushBtn -> { //开启推送通知,跳转到设置界面
                AppUtils.launchAppDetailsSettings()
            }

            switchWechatBtn -> { //开启微信推送开关
                mPresenter.switchSet(
                    4, if (switchWechat.isChecked) {
                        2
                    } else {
                        1
                    }
                )

            }
            wechatPublic -> {//开启微信公众号
                if (!wechatPublicState)
                    SaveQRCodeDialog(this).show()
            }
        }
    }


    //用户广场点赞/评论接收推送开关 参数 type（int）型    1点赞    2评论
    override fun onGreetApproveResult(type: Int, success: Boolean) {
        EventBus.getDefault().post(UpdateSettingEvent())
        when (type) {
            1 -> {
                switchDianzan.isChecked = !switchDianzan.isChecked
            }

            2 -> {
                switchComment.isChecked = !switchComment.isChecked
            }
        }
    }


    //接收推送开关 参数 type（int）型    1短信提醒   4 微信推送
    override fun switchSetResult(type: Int, success: Boolean) {
        EventBus.getDefault().post(UpdateSettingEvent())
        when (type) {
            1 -> {
                switchMessage.isChecked = !switchMessage.isChecked
            }
            4 -> {
                switchWechat.isChecked = !switchWechat.isChecked
                if (switchWechat.isChecked) {
                    wechatPublicTv.isVisible = true
                    wechatPublic.isVisible = true
                    if (wechatPublicState) {
                        wechatPublic.text = getString(R.string.Binded)
                    } else {
                        wechatPublic.text = getString(R.string.Bind_now)
                    }

                    if (switchWechat.isChecked && !wechatPublicState) {
                        SaveQRCodeDialog(this).show()
                    }
                }

            }
        }

    }

    override fun onSettingsBeanResult(success: Boolean, settingsBean: SettingsBean?) {
        if (success) {
            switchDianzan.isChecked = settingsBean!!.notify_square_like_state
            switchComment.isChecked = settingsBean.notify_square_comment_state
            switchMessage.isChecked = settingsBean.sms_state
            if (UserManager.overseas) {
                wechatTv.isVisible = false
                switchWechat.isVisible = false
                switchWechatBtn.isVisible = false
                wechatPublicTv.isVisible = false
                wechatPublic.isVisible = false
            } else {
                wechatPublicState = settingsBean.we_openid
                wechatState = settingsBean.wechat_tem_state
                switchWechat.isChecked = wechatState
                wechatPublicTv.isVisible = wechatState
                wechatPublic.isVisible = wechatState

                if (wechatPublicState) {
                    wechatPublic.text = getString(R.string.Binded)
                } else {
                    wechatPublic.text = getString(R.string.Bind_now)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateSettingEvent(event: UpdateWechatSettingsEvent) {
        if (event.isFollowPublic)
            wechatState = true
        wechatPublicState = event.isFollowPublic
        switchWechat.isChecked = wechatState
        wechatPublicTv.isVisible = wechatState
        wechatPublic.isVisible = wechatState

        if (wechatPublicState) {
            wechatPublic.text = getString(R.string.Binded)
        } else {
            wechatPublic.text = getString(R.string.Bind_now)
        }

    }

}
