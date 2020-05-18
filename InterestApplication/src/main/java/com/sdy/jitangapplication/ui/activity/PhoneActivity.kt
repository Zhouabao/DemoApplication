package com.sdy.jitangapplication.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import kotlinx.android.synthetic.main.activity_phone.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 手机登录界面
 */
class PhoneActivity : BaseActivity(), View.OnClickListener {

    private var wxcode: String = ""
    private var login_type: String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        initView()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun initView() {
        countryCode.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
        wxcode = intent.getStringExtra("wxcode") ?: ""
        login_type = intent.getStringExtra("type") ?: "1"
        if (login_type == "3") {
            titleTv.text = "请绑定手机号"
            tipTv.text = "快，组织需要你的手机号"
        } else {
            titleTv.text = "请输入手机号"
            tipTv.text = "快，组织需要你的手机号"
        }

        btnBack.setOnClickListener(this)
        btnLoginQuestion.setOnClickListener(this)
        btnVerifyCode.setOnClickListener(this)
        nickNameClean.setOnClickListener(this)

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable) {
                if (edit.trim().length == 11) {
                    etPhone.clearFocus()
                    KeyboardUtils.hideSoftInput(etPhone)
                }

                if (!edit.trim().isNullOrEmpty()) {
                    etPhone.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
                    nickNameBg.setBackgroundColor(resources.getColor(R.color.colorOrange))
                    nickNameClean.isVisible = true
                } else {
                    etPhone.typeface = Typeface.DEFAULT
                    nickNameBg.setBackgroundColor(resources.getColor(R.color.colorDividerC4))
                    nickNameClean.isVisible = false
                }


                btnVerifyCode.isEnabled =
                    etPhone.text.toString().isNotEmpty() && etPhone.text.toString().length == 11

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

//        btnVerifyCode
//        ScrollSoftKeyBoardUtils.addLayoutListener(rootView, btnVerifyCode)
        etPhone.postDelayed({
            KeyboardUtils.showSoftInput(etPhone, 0)
        }, 200)
        titleTv.postDelayed({
            CommonFunction.startAnimation(titleTv)
        }, 250L)

        tipTv.postDelayed({
            CommonFunction.startAnimation(tipTv)
        }, 300L)

        clPhone.postDelayed({
            CommonFunction.startAnimation(clPhone)
        }, 350L)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnBack -> {
                finish()
            }
            R.id.btnLoginQuestion -> {
                startActivity<LoginHelpActivity>()
            }
            R.id.nickNameClean -> {
                etPhone.setText("")
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
