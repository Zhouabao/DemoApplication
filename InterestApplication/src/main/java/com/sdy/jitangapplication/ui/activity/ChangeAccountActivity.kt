package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.ChangeAccountPresenter
import com.sdy.jitangapplication.presenter.view.ChangeAccountView
import com.sdy.jitangapplication.ui.dialog.LoginOffDialog
import kotlinx.android.synthetic.main.activity_change_account.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 变更账号
 */
class ChangeAccountActivity : BaseMvpActivity<ChangeAccountPresenter>(), ChangeAccountView, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_account)
        initView()
    }

    private fun initView() {
        mPresenter = ChangeAccountPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "变更账号"
        btnBack.setOnClickListener(this)
        verifycodeBtn.setOnClickListener(this)
        confirmChangeBtn.setOnClickListener(this)
        loginOff.setOnClickListener(this)

        loginOff.text = SpanUtils.with(loginOff)
            .append("如需注销当前帐号，请直接选择“")
            .append("注销账号")
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .append("”\n账号一经注销，您的账号将不会再被任何人看到，\n并且聊天记录会被清空，请谨慎操作。")
            .create()

        newPhoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                checkConfirmBtnEnable()
                if (editable.isNotEmpty() && editable.length == 11) {
                    countTimer.onFinish()
                    verifycodeBtn.isEnabled = true
                    verifycodeBtn.text = "获取验证码"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        newVerifyCodeEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                checkConfirmBtnEnable()

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

    }


    /** 倒计时60秒，一次1秒 */
    private val countTimer by lazy {
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                verifycodeBtn.text = "重新获取"
                verifycodeBtn.isEnabled = true
            }

            override fun onTick(p0: Long) {
                verifycodeBtn.text = "${p0 / 1000}秒后重发"
                verifycodeBtn.isEnabled = false
            }

        }
    }

    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            //获取验证码
            verifycodeBtn -> {
                countTimer.start()
            }
            //确认变更
            confirmChangeBtn -> {

            }
            //注销账号
            loginOff -> {
                LoginOffDialog(this).show()
            }


        }
    }


    /**
     * 确认按钮是否可以点击
     */
    fun checkConfirmBtnEnable() {
        confirmChangeBtn.isEnabled =
            newPhoneEt.text.isNotEmpty() && newPhoneEt.text.length == 11 &&
                    newVerifyCodeEt.text.isNotEmpty() && newVerifyCodeEt.text.length == 6
    }
}