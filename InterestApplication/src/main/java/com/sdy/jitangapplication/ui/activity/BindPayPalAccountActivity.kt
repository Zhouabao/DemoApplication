package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAlipayAccountEvent
import com.sdy.jitangapplication.model.Alipay
import com.sdy.jitangapplication.presenter.BindAlipayAccountPresenter
import com.sdy.jitangapplication.presenter.view.BindAlipayAccountView
import kotlinx.android.synthetic.main.activity_bind_paypal_account.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

/**
 * 绑定支付宝
 */
class BindPayPalAccountActivity : BaseMvpActivity<BindAlipayAccountPresenter>(),
    BindAlipayAccountView, TextWatcher {
    private val alipay by lazy { intent.getSerializableExtra("alipay") as Alipay? }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_paypal_account)
        initView()
    }

    private fun initView() {
        mPresenter = BindAlipayAccountPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.onClick {
            finish()
        }
        hotT1.text = getString(R.string.withdraw_paypal)
        rightBtn1.isVisible = true
        rightBtn1.text = getString(R.string.save)
        etPaypalAccount.addTextChangedListener(this)


        rightBtn1.onClick {
            val params = hashMapOf<String, Any>(
                "ali_account" to etPaypalAccount.text.trim().toString()
            )
            mPresenter.saveWithdrawAccount(params)
        }

        if (alipay != null) {
            etPaypalAccount.setText(alipay?.ali_account)
            etPaypalAccount.setSelection(etPaypalAccount.text.length)
            checkConfirm()
        }
    }

    fun checkConfirm() {
        rightBtn1.isEnabled = !etPaypalAccount.text.trim().isNullOrEmpty()
    }

    override fun afterTextChanged(s: Editable?) {
        checkConfirm()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideSoftInput(etPaypalAccount)
    }

    override fun onResume() {
        super.onResume()
        etPaypalAccount.postDelayed({ KeyboardUtils.showSoftInput(etPaypalAccount) }, 200L)
    }

    override fun saveWithdrawAccountResult(success: Boolean, alipay: Alipay?) {
        if (success) {
            EventBus.getDefault().post(GetAlipayAccountEvent(alipay!!))
            finish()
        }

    }
}
