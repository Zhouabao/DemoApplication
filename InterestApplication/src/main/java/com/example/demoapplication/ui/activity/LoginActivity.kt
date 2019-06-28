package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
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

//        initState()
        initView()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        etPhone.postDelayed({
            KeyboardUtils.showSoftInput(etPhone, 0)
        }, 200)

    }

    private fun initView() {
//        etPhone.isFocusable = true
//        etPhone.requestFocus()


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
                finish()
            }
            R.id.btnLoginQuestion -> {
            }
            R.id.btnVerifyCode -> {
                if (RegexUtils.isMobileSimple(etPhone.text.toString())) {
                    startActivity<VerifyCodeActivity>("phone" to etPhone.text.toString())
                } else {
                    toast("请输入正确的手机号!")
                }
            }
        }
    }
}
