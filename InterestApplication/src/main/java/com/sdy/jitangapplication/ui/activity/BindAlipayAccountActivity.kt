package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAlipayAccountEvent
import com.sdy.jitangapplication.model.Alipay
import com.sdy.jitangapplication.presenter.BindAlipayAccountPresenter
import com.sdy.jitangapplication.presenter.view.BindAlipayAccountView
import kotlinx.android.synthetic.main.activity_bind_alipay_account.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

/**
 * 绑定支付宝
 */
class BindAlipayAccountActivity : BaseMvpActivity<BindAlipayAccountPresenter>(),
    BindAlipayAccountView, TextWatcher {
    private val alipay by lazy { intent.getSerializableExtra("alipay") as Alipay? }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_alipay_account)
        initView()
    }

    private fun initView() {
        mPresenter = BindAlipayAccountPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.onClick {
            finish()
        }
        hotT1.text = "绑定支付宝"

        etTelephone.filters = arrayOf(InputFilter.LengthFilter(11))

        etAlipayAccount.addTextChangedListener(this)
        etAlipayName.addTextChangedListener(this)
        etTelephone.addTextChangedListener(this)

        saveBtn.onClick {
            val params = hashMapOf<String, Any>(
                "ali_account" to etAlipayAccount.text.trim().toString(),
                "nickname" to etAlipayName.text.toString(),
                "phone" to etTelephone.text.toString()
            )
            mPresenter.saveWithdrawAccount(params)
        }

        if (alipay != null) {
            etAlipayAccount.setText(alipay?.ali_account)
            etAlipayName.setText(alipay?.nickname)
            etTelephone.setText(alipay?.phone)
            checkConfirm()
        }
    }

    fun checkConfirm() {
        saveBtn.isEnabled = !etAlipayAccount.text.trim().isNullOrEmpty()
                && !etAlipayName.text.trim().isNullOrEmpty()
                && (!etTelephone.text.trim().isNullOrEmpty() && etTelephone.text.length == 11)
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
        KeyboardUtils.hideSoftInput(etAlipayAccount)
    }

    override fun onResume() {
        super.onResume()
        etAlipayAccount.postDelayed({ KeyboardUtils.showSoftInput(etAlipayAccount) }, 200L)
    }

    override fun saveWithdrawAccountResult(success: Boolean, alipay: Alipay?) {
        if (success) {
            EventBus.getDefault().post(GetAlipayAccountEvent(alipay!!))
            finish()
        }

    }
}