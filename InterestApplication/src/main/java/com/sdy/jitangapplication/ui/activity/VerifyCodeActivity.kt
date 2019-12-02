package com.sdy.jitangapplication.ui.activity

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
import com.sdy.jitangapplication.presenter.VerifyCodePresenter
import com.sdy.jitangapplication.presenter.view.VerifyCodeView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.LoginOffSuccessDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.VerificationCodeInput
import kotlinx.android.synthetic.main.activity_verify_code.*
import org.jetbrains.anko.startActivity


/**
 * 填写验证码界面
 *
 * //todo 新增账号注销界面，待完善注销逻辑:填写验证码之后给一个提示  然后跳转到登录/注册的首页
 */
class VerifyCodeActivity : BaseMvpActivity<VerifyCodePresenter>(), View.OnClickListener, VerifyCodeView {

    private lateinit var verifyCode: String
    private val phone by lazy { intent.getStringExtra("phone") }

    private val loadingDialog by lazy { LoadingDialog(this) }

    companion object {
        const val TYPE_LOGIN_PHONE = 1 //手机登录
        const val TYPE_LOGIN_WECHAT = 3//微信登录
        const val TYPE_LOGIN_OFF = 2//注销
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
                tv1.text = "验证码"
                help.isVisible = false

            }

            "${TYPE_LOGIN_OFF}" -> {
                tv1.text = "注销账号"
                help.isVisible = true
            }
        }
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
                    if (!loadingDialog.isShowing)
                        loadingDialog.show()
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

    override fun onConfirmVerifyCode(data: LoginBean?, isRight: Boolean) {
        if (isRight) {
            //注销成功
            if (intent.getStringExtra("type") == "$TYPE_LOGIN_OFF") {
                loadingDialog.dismiss()
                LoginOffSuccessDialog(this).show()
            } else {//登录成功
                this.data = data
                mPresenter.loginIM(LoginInfo(data!!.accid, data!!.extra_data?.im_token))
            }
        } else {
            loadingDialog.dismiss()
            inputVerifyCode.isEnabled = true
        }
    }


    override fun onGetVerifyCode(data: BaseResp<Any?>?) {
        if (data != null && data.code == 200) {
            tvPhone.text = "验证码已发至 $phone"
            countVerifyCodeTime.isEnabled = false
            onCountTime()
        } else {
            countVerifyCodeTime.isEnabled = true
        }
    }


    /**
     * IM登录
     */
    override fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        if (success) {
            UserManager.startToPersonalInfoActivity(this, nothing, data)
        } else {
            CommonFunction.toast("登录失败！请重试")
            loadingDialog.dismiss()
            inputVerifyCode.isEnabled = true
        }
    }


    override fun onError(text: String) {
        CommonFunction.toast(text)
    }
}
