package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAlipayAccountEvent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_alipay_account)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        hotT1.text = "绑定支付宝"

        etAlipayAccount.addTextChangedListener(this)
        etAlipayName.addTextChangedListener(this)
        etTelephone.addTextChangedListener(this)

        saveBtn.onClick {
            EventBus.getDefault().post(GetAlipayAccountEvent(etAlipayAccount.text.trim().toString()))
            finish()
        }
    }

    fun checkConfirm() {
        saveBtn.isEnabled = !etAlipayAccount.text.trim().isNullOrEmpty()
                && !etAlipayName.text.trim().isNullOrEmpty()
                && !etTelephone.text.trim().isNullOrEmpty()
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
}
