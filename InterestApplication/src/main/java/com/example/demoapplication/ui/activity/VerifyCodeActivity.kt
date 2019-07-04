package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.example.demoapplication.R
import com.example.demoapplication.presenter.VerifyCodePresenter
import com.example.demoapplication.presenter.view.VerifyCodeView
import com.kotlin.base.data.protocol.BaseResp
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
    private val phone by lazy { intent.getStringExtra("phone") }

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
                tvPhone.text = "已发送至 $phone"
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
                    toast("请输入验证码！")
                    return
                } else {
                    mPresenter.checkVerifyCode(phone, verifyCode)
                }
            }
            R.id.countVerifyCodeTime -> {
                mPresenter.getVerifyCode(phone)
            }
        }
    }

    override fun onConfirmVerifyCode(isRight: Boolean) {
        if (isRight) {
            startActivity<SetInfoActivity>()
            finish()
        } else {
            toast("验证码输入不正确！")
            onChangeVerifyButtonStatus(false)
        }
    }


    override fun onGetVerifyCode(data: BaseResp<Array<String>?>) {
        if (data.code == 200) {
            countVerifyCodeTime.isEnabled = false
            onCountTime()
        }
    }
}
