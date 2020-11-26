package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.loginOffCauseBean
import com.sdy.jitangapplication.presenter.ChangeAccountPresenter
import com.sdy.jitangapplication.presenter.view.ChangeAccountView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.LoginOffDialog
import kotlinx.android.synthetic.main.activity_change_account.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivityForResult

/**
 * 变更账号
 */
class ChangeAccountActivity : BaseMvpActivity<ChangeAccountPresenter>(), ChangeAccountView,
    View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_account)
        initView()
    }


    private fun initView() {
        mPresenter = ChangeAccountPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = getString(R.string.account_change_title)
        btnBack.setOnClickListener(this)
        verifycodeBtn.setOnClickListener(this)
        confirmChangeBtn.setOnClickListener(this)
        loginOff.setOnClickListener(this)
        countryCodeTv.setOnClickListener(this)

        loginOff.text = SpanUtils.with(loginOff)
            .append(getString(R.string.account_login_offtip1))
            .append(getString(R.string.account_login_offtip2))
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .append(getString(R.string.account_login_offtip3))
            .create()

        newPhoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                checkConfirmBtnEnable()

                if (editable.isNotEmpty() && editable.length == 11) {
                    countTimer.onFinish()
                    verifycodeBtn.isEnabled = true
                    verifycodeBtn.text = getString(R.string.get_verify_code)
                } else {
                    verifycodeBtn.isEnabled = false
                    verifycodeBtn.text = getString(R.string.get_verify_code)
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        newVerifyCodeEt.addTextChangedListener(
            object : TextWatcher {
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
                verifycodeBtn.text = getString(R.string.reget_verify_code)
                verifycodeBtn.isEnabled = true
            }

            override fun onTick(p0: Long) {
                verifycodeBtn.text = "${p0 / 1000}${getString(R.string.seconds_resend)}"
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
                mPresenter.sendSms(
                    hashMapOf<String, Any>(
                        "phone" to newPhoneEt.text.toString(),
                        "scene" to "register",
                        "region" to countryCode
                    )
                )
            }
            //确认变更
            confirmChangeBtn -> {
                mPresenter.changeAccount(
                    hashMapOf<String, Any>(
                        "uni_account" to newPhoneEt.text.toString(),
                        "code" to newVerifyCodeEt.text.toString()
                    )
                )
            }
            //注销账号
            loginOff -> {
                mPresenter.getCauseList()
            }

            //选择区号
            countryCodeTv -> {
                startActivityForResult<CountryCodeActivity>(1002)
            }
        }
    }


    private var countryCode = 86
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1002) {
            countryCode = data?.getIntExtra("code", 86) ?: 86
        }
    }


    /**
     * 确认按钮是否可以点击
     */
    fun checkConfirmBtnEnable() {
        confirmChangeBtn.isEnabled =
            newPhoneEt.text.isNotEmpty() && newPhoneEt.text.length == 11
                    && newVerifyCodeEt.text.isNotEmpty() && newVerifyCodeEt.text.length == 4
    }


    override fun onChangeAccountResult(result: Boolean) {
        if (result) {
            setResult(Activity.RESULT_OK, intent.putExtra("phone", newPhoneEt.text.toString()))
            finish()
        }

    }


    override fun onSendSmsResult(result: Boolean) {
        countTimer.start()
    }

    override fun onCauseListResult(result: loginOffCauseBean) {
        LoginOffDialog(this, intent.getStringExtra("phone"), result).show()
    }


    private val loadingDialog by lazy { LoadingDialog(this) }
    override fun showLoading() {
        if (!loadingDialog.isShowing)
            loadingDialog.show()
    }

    override fun hideLoading() {
        if (loadingDialog.isShowing)
            loadingDialog.dismiss()
    }


}
