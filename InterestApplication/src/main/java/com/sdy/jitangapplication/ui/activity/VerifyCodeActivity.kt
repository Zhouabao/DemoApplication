package com.sdy.jitangapplication.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.RegisterTooManyBean
import com.sdy.jitangapplication.presenter.VerifyCodePresenter
import com.sdy.jitangapplication.presenter.view.VerifyCodeView
import com.sdy.jitangapplication.ui.dialog.LoginOffSuccessDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.VerificationCodeInput
import kotlinx.android.synthetic.main.activity_verify_code.*
import org.jetbrains.anko.startActivity


/**
 * 填写验证码界面
 *
 * // 新增账号注销界面，待完善注销逻辑:填写验证码之后给一个提示  然后跳转到登录/注册的首页
 */
class VerifyCodeActivity : BaseMvpActivity<VerifyCodePresenter>(), View.OnClickListener,
    VerifyCodeView {

    private lateinit var verifyCode: String
    private val phone by lazy { intent.getStringExtra("phone") }


    companion object {
        const val TYPE_LOGIN_PHONE = 1 //手机登录
        const val TYPE_LOGIN_OFF = 2//注销
        const val TYPE_LOGIN_WECHAT = 3//微信登录
        const val TYPE_LOGIN_SY = 4//闪验登录
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        initView()
    }


    private fun initView() {
        mPresenter = VerifyCodePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        when (intent.getStringExtra("type")) {
            "$TYPE_LOGIN_PHONE", "$TYPE_LOGIN_WECHAT" -> {
                tv1.text = resources.getString(R.string.verify_code)
                help.isVisible = false

            }

            "$TYPE_LOGIN_OFF" -> {
                tv1.text = resources.getString(R.string.login_off)
                help.isVisible = true
            }
        }
        btnBack.onClick { finish() }
        changePhone.setOnClickListener(this)
        help.setOnClickListener(this)
        countVerifyCodeTime.setOnClickListener(this)
        //设置手机号
        onGetPhoneNum()
        mPresenter.getVerifyCode(phone)
        inputVerifyCode.isEnabled = true
        inputVerifyCode.setOnCompleteListener(object : VerificationCodeInput.Listener {
            override fun onComplete(complete: Boolean, content: String?) {
                if (complete) {
                    if (intent.getStringExtra("type") == "$TYPE_LOGIN_OFF") {
                        mPresenter.cancelAccount(
                            hashMapOf<String, Any>(
                                "uni_account" to intent.getStringExtra("phone"),
                                "code" to content!!,
                                "descr" to intent.getStringExtra("descr")
                            )
                        )
                    } else
                        mPresenter.checkVerifyCode(
                            intent.getStringExtra("wxcode") ?: "",
                            intent.getStringExtra("type") ?: "1",
                            phone,
                            content!!
                        )
                }
            }
        })

        CommonFunction.startAnimation(tv1)
        clSend.postDelayed({
            CommonFunction.startAnimation(clSend)
        }, 50L)
        inputVerifyCode.postDelayed({
            CommonFunction.startAnimation(inputVerifyCode)
        }, 100L)
    }

    /** 倒计时60秒，一次1秒 */
    val timer = object : CountDownTimer(60 * 1000, 1000) {
        override fun onFinish() {
            tvPhone.text = phone
            SpanUtils.with(countVerifyCodeTime)
                .append(resources.getString(R.string.reget_verify_code))
                .setBold()
                .create()
            countVerifyCodeTime.isEnabled = true
        }

        override fun onTick(p0: Long) {
            SpanUtils.with(countVerifyCodeTime)
                .append(resources.getString(R.string.verify_code_has_send))
                .append("  ${p0 / 1000}${resources.getString(R.string.second)}")
                .setBold()
                .create()
        }

    }

    override fun onCountTime() {
        timer.cancel()
        timer.start()
    }

    override fun onGetPhoneNum() {
        tvPhone.text = phone
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

    override fun onConfirmVerifyCode(data: LoginBean?, isRight: Boolean) {
        if (isRight) {
            //注销成功
            if (intent.getStringExtra("type") == "$TYPE_LOGIN_OFF") {
                LoginOffSuccessDialog(this).show()
            } else {//登录成功
                this.data = data
                mPresenter.loginIM(LoginInfo(data!!.accid, data.extra_data?.im_token))
            }
        } else {
            inputVerifyCode.isEnabled = true
        }
    }


    override fun onGetVerifyCode(data: BaseResp<RegisterTooManyBean?>?) {
        if (data != null && data.code == 200) {
            SpanUtils.with(tvPhone)
                .append("${resources.getString(R.string.verify_code_sended)} ")
                .append(phone)
                .setTypeface(Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf"))
                .setFontSize(18, true)
                .create()
            countVerifyCodeTime.isEnabled = false
            onCountTime()
        } else if (data?.code == 401) {
            RegisterTooManyActivity.start(data.data?.countdown_time ?: 0, this)
            countVerifyCodeTime.isEnabled = true
            SpanUtils.with(countVerifyCodeTime)
                .append(resources.getString(R.string.reget_verify_code))
                .setBold()
                .create()
        } else {
            CommonFunction.toast("${data?.msg}")
            countVerifyCodeTime.isEnabled = true
            SpanUtils.with(countVerifyCodeTime)
                .append(resources.getString(R.string.reget_verify_code))
                .setBold()
                .create()
        }
    }


    /**
     * IM登录
     */
    override fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        inputVerifyCode.isEnabled = true
        if (success) {
            UserManager.startToPersonalInfoActivity(this, nothing, data)
        } else {
            CommonFunction.toast(resources.getString(R.string.login_error))
        }

    }


    override fun onError(text: String) {
        CommonFunction.toast(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}
