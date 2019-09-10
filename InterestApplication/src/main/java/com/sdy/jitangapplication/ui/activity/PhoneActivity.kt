package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.LoginPresenter
import com.sdy.jitangapplication.presenter.view.LoginView
import kotlinx.android.synthetic.main.activity_phone.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 手机登录界面
 */
class PhoneActivity : BaseMvpActivity<LoginPresenter>(), LoginView, View.OnClickListener {

    private var wxcode: String = ""
    private var login_type: String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        initView()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        etPhone.postDelayed({
            KeyboardUtils.showSoftInput(etPhone, 0)
        }, 200)

    }

    private fun initView() {
        wxcode = intent.getStringExtra("wxcode") ?: ""
        login_type = intent.getStringExtra("type") ?: "1"
        if (login_type == "3") {
            titleTv.text = "请绑定手机号"
        } else {
            titleTv.text = "请输入手机号"
        }


        mPresenter = LoginPresenter()
        mPresenter.mView = this
        mPresenter.context = this


        btnBack.setOnClickListener(this)
        btnLoginQuestion.setOnClickListener(this)
        btnVerifyCode.setOnClickListener(this)

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable?) {
                btnVerifyCode.isEnabled = etPhone.text.toString().isNotEmpty() && etPhone.text.toString().length == 11

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnBack -> {
                finish()
            }
            R.id.btnLoginQuestion -> {
            }
            R.id.btnVerifyCode -> {
                if (RegexUtils.isMobileSimple(etPhone.text.toString())) {
                    startActivity<VerifyCodeActivity>(
                        "phone" to etPhone.text.toString(),
                        "wxcode" to wxcode,
                        "type" to login_type
                    )
                } else {
                    toast("请输入正确的手机号!")
                }
            }
        }
    }

}
