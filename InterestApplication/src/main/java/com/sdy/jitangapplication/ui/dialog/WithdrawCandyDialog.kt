package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAlipayAccountEvent
import com.sdy.jitangapplication.ui.activity.BindAlipayAccountActivity
import kotlinx.android.synthetic.main.dialog_withdraw_candy.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 糖果提现人民币
 *    version: 1.0
 */
class WithdrawCandyDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog),
    View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_withdraw_candy)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(false)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }


    private fun initView() {
        inputWithdrawMoney.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkWithdrawEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        withdrawAll.setOnClickListener(this)
        wirteAlipayAcount.setOnClickListener(this)
        confirmWithdraw.setOnClickListener(this)
        successBtn.setOnClickListener(this)
    }


    fun checkWithdrawEnable() {
        confirmWithdraw.isEnabled = !inputWithdrawMoney.text.trim().isNullOrEmpty()
                && !wirteAlipayAcount.text.trim().isNullOrEmpty()
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetAlipayAccountEvent(event: GetAlipayAccountEvent) {
        wirteAlipayAcount.text = event.account
        checkWithdrawEnable()
    }

    private val loadingDialog by lazy { LoadingDialog(myContext) }
    override fun onClick(v: View) {
        when (v.id) {
            R.id.withdrawAll -> {//全部提现

            }
            R.id.wirteAlipayAcount -> {//填写支付宝账户
                myContext.startActivity<BindAlipayAccountActivity>()

            }
            R.id.successBtn -> {//提现成功
                dismiss()
            }
            R.id.confirmWithdraw -> {//确认提现
                loadingDialog.show()
                withdrawCl.postDelayed({
                    withdrawCl.isVisible = false
                    loadingDialog.dismiss()
                    withdrawSuccessCl.isVisible = true
                }, 1000L)
            }

        }

    }


}