package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.kotlin.base.common.AppManager
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.presenter.VerifyCodePresenter
import com.sdy.jitangapplication.presenter.view.VerifyCodeView
import com.sdy.jitangapplication.receiver.SMSBroadcastReceiver
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_verify_code.*
import org.jetbrains.anko.startActivity


/**
 * 填写验证码界面
 */
class VerifyCodeActivity : BaseMvpActivity<VerifyCodePresenter>(), VerifyCodeView, View.OnClickListener,
    SMSBroadcastReceiver.OnReceivedSMSListener {

    private lateinit var verifyCode: String
    private val phone by lazy { intent.getStringExtra("phone") }
    //监听短信的广播
//    private val mSMSBroadcastReceiver = SMSBroadcastReceiver(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        initView()
    }

    private fun initView() {
        // 注册广播
//        val intentFilter = IntentFilter(SMSBroadcastReceiver.SMS_RECEIVED_ACTION)
//        intentFilter.priority = Integer.MAX_VALUE
//        registerReceiver(mSMSBroadcastReceiver, intentFilter)

        mPresenter = VerifyCodePresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        btnVerifyCode.setOnClickListener(this)
        countVerifyCodeTime.setOnClickListener(this)
        //设置手机号
        onGetPhoneNum()
        //获取验证码
        mPresenter.getVerifyCode(phone)

        clVerifyCode.listener = { verifyCode, complete ->
            if (complete) {
                this.verifyCode = verifyCode
                onChangeVerifyButtonStatus(true)
            } else {
                onChangeVerifyButtonStatus(false)
            }
        }

    }

    override fun onReceived(message: String) {
        Log.d("SMSBroadcastReceiver", message)
    }


    override fun onChangeVerifyButtonStatus(enable: Boolean) {
        btnVerifyCode.isEnabled = enable
    }


    override fun onCountTime() {
        /** 倒计时60秒，一次1秒 */
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                tvPhone.text = "+86 $phone"
                countVerifyCodeTime.text =
                    SpanUtils.with(countVerifyCodeTime).append("验证码已发送").append("  重新获取").setBold().create()
                countVerifyCodeTime.isEnabled = true
            }

            override fun onTick(p0: Long) {
                countVerifyCodeTime.text =
                    SpanUtils.with(countVerifyCodeTime).append("验证码已发送").append("  ${p0 / 1000}秒").setBold().create()
            }

        }.start()
    }

    override fun onGetPhoneNum() {
        tvPhone.text = "+86 $phone"
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnVerifyCode -> {
                if (verifyCode.length != 6) {
                    CommonFunction.toast("请输入验证码！")
                    return
                } else {
                    mPresenter.checkVerifyCode(
                        intent.getStringExtra("wxcode") ?: "",
                        intent.getStringExtra("type") ?: "1",
                        phone,
                        verifyCode
                    )
                    onChangeVerifyButtonStatus(false)
                }
            }
            R.id.countVerifyCodeTime -> {
                mPresenter.getVerifyCode(phone)
            }
        }
    }

    /**
     * 此时登录成功
     *      // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录）
    NIMClient.init(this, loginInfo(), options())
     */
    private var data: LoginBean? = null

    override fun onConfirmVerifyCode(data: LoginBean, isRight: Boolean) {
        onChangeVerifyButtonStatus(true)
        if (isRight) {
            this.data = data
            mPresenter.loginIM(LoginInfo(data.accid, data.extra_data?.im_token))

        }
    }


    override fun onGetVerifyCode(data: BaseResp<Any?>) {
        tvPhone.text = "已发送至+86 $phone"
        countVerifyCodeTime.isEnabled = false
        onCountTime()
    }


    /**
     * IM登录
     */
    override fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        if (success) {
            SPUtils.getInstance(Constants.SPNAME).put("imToken", nothing?.token)
            SPUtils.getInstance(Constants.SPNAME).put("imAccid", nothing?.account)

            SPUtils.getInstance(Constants.SPNAME).put("qntoken", data?.qntk)
            SPUtils.getInstance(Constants.SPNAME).put("token", data?.token)
            SPUtils.getInstance(Constants.SPNAME).put("accid", data?.accid)
            DemoCache.setAccount(nothing?.account)
            //初始化消息提醒配置
            initNotificationConfig()

            if (data != null && data!!.userinfo != null
                && (data!!.userinfo!!.nickname.isNullOrEmpty() || data!!.userinfo!!.avatar.isNullOrEmpty()
                        || data!!.userinfo!!.gender == 0 || data!!.userinfo!!.birth.isNullOrEmpty())
            ) {//个人信息没有填写
                startActivity<SetInfoActivity>()
            } else {
                UserManager.saveUserInfo(data!!)
                if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels") == null || SPUtils.getInstance(
                        Constants.SPNAME
                    ).getStringSet("checkedLabels").isEmpty()
                ) {//标签没有选择
                    startActivity<NewLabelsActivity1>()
                } else {//跳到主页
                    AppManager.instance.finishAllActivity()
                    startActivity<MainActivity>()
                }
            }
        } else {
            CommonFunction.toast("登录失败！请重试")
        }
    }

    private fun initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
        // 加载状态栏配置
        var statusBarNotificationConfig = UserPreferences.getStatusConfig()
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig()
            UserPreferences.setStatusConfig(statusBarNotificationConfig)
        }
        //更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig)

    }

    override fun onError(text: String) {
        ToastUtils.showShort(text)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
