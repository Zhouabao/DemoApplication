package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.demoapplication.R
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 手机登录界面
 */
class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initState()
        initView()
    }

    private fun initView() {
        btnBack.setOnClickListener(this)
        btnLoginQuestion.setOnClickListener(this)
        btnVerifyCode.setOnClickListener(this)

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable?) {
                if (etPhone.text.toString().isNotEmpty() && etPhone.text.toString().length == 11) {
                    btnVerifyCode.isEnabled = true
                } else {

                }

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
                startActivity<WelcomeActivity>()
            }
            R.id.btnLoginQuestion -> {
            }
            R.id.btnVerifyCode -> {
                if (etPhone.text.toString().isEmpty()) {
                    toast("请输入手机号！")
                    return
                }
                if (etPhone.text.toString().length != 11) {
                    toast("请输入正确的手机号!")
                    return
                }
                startActivity<VerifyCodeActivity>("phone" to etPhone.text.toString())
            }
        }
    }
}
