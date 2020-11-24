package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.RegexUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import kotlinx.android.synthetic.main.activity_phone.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

/**
 * 手机登录界面
 */
class PhoneActivity : BaseActivity(), OnLazyClickListener {

    private var wxcode: String = ""
    private var login_type: String = "1"

    companion object {
        const val REQUEST_FOR_COUNTRY_CODE = 1000
    }

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
            titleTv.text = getString(R.string.please_bind_phone)
            tipTv.text = getString(R.string.hurry_make_phone)
        } else {
            titleTv.text = getString(R.string.please_bind_phone)
            tipTv.text = getString(R.string.hurry_make_phone)
        }

        btnBack.setOnClickListener(this)
        btnLoginQuestion.setOnClickListener(this)
        btnVerifyCode.setOnClickListener(this)
        nickNameClean.setOnClickListener(this)
        countryCode.setOnClickListener(this)

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


    override fun onLazyClick(view: View) {
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
                    toast(getString(R.string.please_input_correct_phone))
                }
            }
            R.id.countryCode -> {
                startActivityForResult<CountryCodeActivity>(REQUEST_FOR_COUNTRY_CODE)
                KeyboardUtils.hideSoftInputByToggle(this)
            }

        }
    }


    private var nowCountryCode = 86
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_FOR_COUNTRY_CODE && data != null) {
                nowCountryCode = data.getIntExtra("code", 86)
                countryCode.text = "+${nowCountryCode}"
            }
    }
}
