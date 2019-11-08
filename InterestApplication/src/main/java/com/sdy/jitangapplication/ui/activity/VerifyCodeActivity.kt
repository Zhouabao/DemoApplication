package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SpanUtils
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
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.VerificationCodeInput
import kotlinx.android.synthetic.main.activity_verify_code.*
import org.jetbrains.anko.startActivity


/**
 * 填写验证码界面
 */
class VerifyCodeActivity : BaseMvpActivity<VerifyCodePresenter>(), View.OnClickListener, VerifyCodeView {

    private lateinit var verifyCode: String
    private val phone by lazy { intent.getStringExtra("phone") }

    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        initView()
    }

    private fun initView() {
        mPresenter = VerifyCodePresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        changePhone.setOnClickListener(this)
        help.setOnClickListener(this)
        countVerifyCodeTime.setOnClickListener(this)
        //设置手机号
        onGetPhoneNum()
        //获取验证码
        mPresenter.getVerifyCode(phone)

        inputVerifyCode.isEnabled = true
        inputVerifyCode.setOnCompleteListener(object : VerificationCodeInput.Listener {
            override fun onComplete(complete: Boolean, content: String?) {
                if (complete) {
                    loadingDialog.show()
                    mPresenter.checkVerifyCode(
                        intent.getStringExtra("wxcode") ?: "",
                        intent.getStringExtra("type") ?: "1",
                        phone,
                        content!!
                    )
                }
            }

        })

    }


    override fun onCountTime() {
        /** 倒计时60秒，一次1秒 */
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                tvPhone.text = "$phone"
                countVerifyCodeTime.text = SpanUtils.with(countVerifyCodeTime)
                    .append("重新获取")
                    .setBold()
                    .create()
                countVerifyCodeTime.isEnabled = true
            }

            override fun onTick(p0: Long) {
                countVerifyCodeTime.text =
                    SpanUtils.with(countVerifyCodeTime)
                        .append("验证码已发送")
                        .append("  ${p0 / 1000}秒")
                        .setBold()
                        .create()
            }

        }.start()
    }

    override fun onGetPhoneNum() {
        tvPhone.text = "$phone"
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.countVerifyCodeTime -> {
                mPresenter.getVerifyCode(phone)
            }
            R.id.changePhone -> {
                onBackPressed()
            }
            R.id.help -> {
                startActivity<LoginHelpActivity>()
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
        if (isRight) {
            this.data = data
            mPresenter.loginIM(LoginInfo(data.accid, data.extra_data?.im_token))
        } else {
            loadingDialog.dismiss()
            inputVerifyCode.isEnabled = true
        }
    }


    override fun onGetVerifyCode(data: BaseResp<Any?>) {
        tvPhone.text = "验证码已发至 $phone"
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
                        || data!!.userinfo!!.avatar!!.contains(Constants.DEFAULT_AVATAR)
                        || data!!.userinfo!!.gender == 0 || data!!.userinfo!!.birth.isNullOrEmpty() || data!!.userinfo!!.birth.toLong() == 0L)
            ) {//个人信息没有填写
                startActivity<UserNickNameActivity>()
                finish()
            } else {
                UserManager.saveUserInfo(data!!)
                if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels") == null || SPUtils.getInstance(
                        Constants.SPNAME
                    ).getStringSet("checkedLabels").isEmpty()
                ) {//标签没有选择
                    startActivity<LabelsActivity>()
                } else {//跳到主页
                    AppManager.instance.finishAllActivity()
                    startActivity<MainActivity>()
                }
            }
        } else {
            CommonFunction.toast("登录失败！请重试")
            loadingDialog.dismiss()
            inputVerifyCode.isEnabled = true
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
        CommonFunction.toast(text)
    }
}
