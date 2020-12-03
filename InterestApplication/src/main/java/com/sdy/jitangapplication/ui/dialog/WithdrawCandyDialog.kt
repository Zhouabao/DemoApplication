package com.sdy.jitangapplication.ui.dialog

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.GetAlipayAccountEvent
import com.sdy.jitangapplication.event.RefreshMyCandyEvent
import com.sdy.jitangapplication.event.RefreshMyWithDraw
import com.sdy.jitangapplication.model.PullWithdrawBean
import com.sdy.jitangapplication.model.WithDrawSuccessBean
import com.sdy.jitangapplication.ui.activity.BindAlipayAccountActivity
import com.sdy.jitangapplication.ui.activity.BindPayPalAccountActivity
import com.sdy.jitangapplication.utils.UserManager
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
class WithdrawCandyDialog(val myContext: Context, val fromCandy: Boolean = true) :
    BottomSheetDialog(myContext, R.style.MyDialog),
    View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_withdraw_candy)
        initWindow()
        initView()
        pullWithdraw()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }


    private fun initView() {
        if (fromCandy) {
            t7.text = myContext.getString(R.string.exchange_money)
            candyCount.isVisible = true
        } else {
            t7.text = myContext.getString(R.string.withdraw_money)
            candyCount.visibility = View.INVISIBLE
        }
        t8.isVisible = fromCandy
        withdrawCandy.isVisible = fromCandy
        inputWithdrawMoney.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty())
                    if (s.toString().toFloat() > if (fromCandy) {
                            pullWithdrawBean?.money_amount ?: 0F
                        } else {
                            pullWithdrawBean?.red_balance_money ?: 0F
                        }
                    ) {

                        CommonFunction.toast(
                            myContext.getString(
                                R.string.withdraw_max, if (fromCandy) {
                                    pullWithdrawBean?.money_amount ?: 0F
                                } else {
                                    pullWithdrawBean?.red_balance_money ?: 0F
                                }
                            )
                        )
                        inputWithdrawMoney.setText("")
                    }
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

        if (UserManager.overseas) {
            t4.text = myContext.getString(R.string.withdraw_paypal)
        } else {
            t4.text = myContext.getString(R.string.alipay)
        }
    }


    fun checkWithdrawEnable() {
        confirmWithdraw.isEnabled = !inputWithdrawMoney.text.trim().isNullOrEmpty() &&
                !wirteAlipayAcount.text.trim().isNullOrEmpty() &&
                if (inputWithdrawMoney.text.isNullOrEmpty()) {
                    0F
                } else {
                    inputWithdrawMoney.text.toString().toFloat()
                } <= (if (fromCandy) {
            pullWithdrawBean?.money_amount ?: 0F
        } else {
            pullWithdrawBean?.red_balance_money ?: 0F
        })
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
        wirteAlipayAcount.text = event.account.ali_account
        checkWithdrawEnable()
    }

    private val loadingDialog by lazy { LoadingDialog(myContext) }
    override fun onClick(v: View) {
        when (v.id) {
            R.id.withdrawAll -> {//全部提现
                inputWithdrawMoney.setText(
                    "${if (fromCandy) {
                        pullWithdrawBean?.money_amount
                    } else {
                        pullWithdrawBean?.red_balance_money
                    }}"
                )
                inputWithdrawMoney.setSelection(inputWithdrawMoney.text.length)
            }
            R.id.wirteAlipayAcount -> {//填写支付宝账户
                if (UserManager.overseas)
                    myContext.startActivity<BindPayPalAccountActivity>("alipay" to pullWithdrawBean?.alipay)
                else
                    myContext.startActivity<BindAlipayAccountActivity>("alipay" to pullWithdrawBean?.alipay)
            }
            R.id.successBtn -> {//提现成功
                dismiss()
            }
            R.id.confirmWithdraw -> {//确认提现
                withdraw()
            }
        }
    }

    private var pullWithdrawBean: PullWithdrawBean? = null

    /**
     * 拉起提现
     */
    fun pullWithdraw() {
        if (fromCandy)
            RetrofitFactory.instance.create(Api::class.java)
                .myCadny(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<PullWithdrawBean?>>(null) {
                    override fun onNext(t: BaseResp<PullWithdrawBean?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            pullWithdrawBean = t.data
                            candyCount.text = "${pullWithdrawBean?.candy_amount}"
                            withdrawMoney.text = myContext.getString(
                                R.string.can_withdraw,
                                pullWithdrawBean?.money_amount
                            )
                            if (pullWithdrawBean?.alipay != null)
                                wirteAlipayAcount.text = pullWithdrawBean?.alipay?.ali_account
                        }
                    }
                })
        else
            RetrofitFactory.instance.create(Api::class.java)
                .pullWithdraw(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<PullWithdrawBean?>>(null) {
                    override fun onNext(t: BaseResp<PullWithdrawBean?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            pullWithdrawBean = t.data
                            withdrawMoney.text = myContext.getString(
                                R.string.can_withdraw,
                                pullWithdrawBean?.red_balance_money
                            )
                            if (pullWithdrawBean?.alipay != null)
                                wirteAlipayAcount.text = pullWithdrawBean?.alipay?.ali_account
                        }
                    }
                })
    }


    /**
     * 提现
     */
    fun withdraw() {
        val params = hashMapOf<String, Any>(
            "amount" to inputWithdrawMoney.text.toString().toFloat()
        )
        if (fromCandy)
            RetrofitFactory.instance.create(Api::class.java)
                .withdraw(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<WithDrawSuccessBean?>>(null) {
                    override fun onStart() {
                        super.onStart()
                        loadingDialog.show()
                    }

                    override fun onNext(t: BaseResp<WithDrawSuccessBean?>) {
                        super.onNext(t)
                        loadingDialog.dismiss()
                        if (t.code == 200) {
                            withdrawCl.isVisible = false
                            withdrawSuccessCl.isVisible = true
                            withdrawID.text = "${t.data?.trade_no}"
                            withdrawTime.text = "${t.data?.create_tme}"
                            withdrawCandy.text =
                                t.data?.candy_amount.toString() + context.getString(
                                    R.string.count_unit
                                )
                            withdrawMoney1.text = "${CommonFunction.getNowMoneyUnit()}${t.data?.money_amount}"
                            EventBus.getDefault()
                                .post(RefreshMyCandyEvent(t.data?.candy_amount ?: 0))
                        } else {
                            CommonFunction.toast(t.msg)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        loadingDialog.dismiss()
                    }
                })
        else
            RetrofitFactory.instance.create(Api::class.java)
                .doRedWithdraw(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<WithDrawSuccessBean?>>(null) {
                    override fun onStart() {
                        super.onStart()
                        loadingDialog.show()
                    }

                    override fun onNext(t: BaseResp<WithDrawSuccessBean?>) {
                        super.onNext(t)
                        loadingDialog.dismiss()
                        if (t.code == 200) {
                            withdrawCl.isVisible = false
                            withdrawSuccessCl.isVisible = true
                            withdrawID.text = "${t.data?.trade_no}"
                            withdrawTime.text = "${t.data?.create_tme}"
                            withdrawCandy.text = "${t.data?.candy_amount}${context.getString(
                                R.string.count_unit
                            )}"
                            withdrawMoney1.text = "${CommonFunction.getNowMoneyUnit()}${t.data?.money_amount}"
                            EventBus.getDefault()
                                .post(RefreshMyWithDraw(t.data?.money_amount ?: 0F))
                        } else {
                            CommonFunction.toast(t.msg)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        loadingDialog.dismiss()
                    }
                })
    }


}