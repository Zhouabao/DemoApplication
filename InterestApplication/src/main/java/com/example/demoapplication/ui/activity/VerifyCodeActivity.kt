package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.example.demoapplication.R
import com.example.demoapplication.presenter.VerifyCodePresenter
import com.example.demoapplication.presenter.view.VerifyCodeView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_verify_code.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


/**
 * 填写验证码界面
 */
class VerifyCodeActivity : BaseMvpActivity<VerifyCodePresenter>(), VerifyCodeView, View.OnClickListener {
    private lateinit var verifyCode: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        initView()
    }

    private fun initView() {
        mPresenter = VerifyCodePresenter()
        mPresenter.mView = this
        btnBack.onClick { finish() }
        btnVerifyCode.setOnClickListener(this)
        countVerifyCodeTime.setOnClickListener(this)
        onGetPhoneNum()
        onCountTime()

        clVerifyCode.listener = { verifyCode, complete ->
            if (complete) {
                this.verifyCode = verifyCode
                onChangeVerifyButtonStatus(true)
            }
        }

    }


    override fun onChangeVerifyButtonStatus(enable: Boolean) {
        btnVerifyCode.isEnabled = enable
    }

    override fun onCountTime() {
        /** 倒计时60秒，一次1秒 */
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                countVerifyCodeTime.text = "验证码已发送 重新获取"
                countVerifyCodeTime.isEnabled = true
            }

            override fun onTick(p0: Long) {
                countVerifyCodeTime.text = "验证码已发送  ${p0 / 1000}秒"
            }

        }.start()


    }

    override fun onGetPhoneNum() {
        tvPhone.text = "+86 ${intent.getStringExtra("phone")}"
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnVerifyCode -> {
                toast(verifyCode)
                if (verifyCode.length != 6) {
                    toast("请输入验证码！")
                    return
                } else {
                    mPresenter.getVerifyCode(verifyCode)
                }
            }
            R.id.countVerifyCodeTime -> {
                //todo 重新发送验证码请求
                countVerifyCodeTime.isEnabled = false
                onCountTime()
            }
        }
    }

    override fun onConfirmVerifyCode(isRight: Boolean) {
        if (isRight) {
            startActivity<SetInfoActivity>()
        } else {
            toast("验证码输入不正确！")
            onChangeVerifyButtonStatus(false)
        }
    }


}
