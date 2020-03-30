package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.Paylist
import com.sdy.jitangapplication.model.RechargeBean
import com.sdy.jitangapplication.ui.adapter.CandyPriceAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_recharge_candy_discount.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 糖果充值价格获取
 *    version: 1.0
 */
class RechargeCandyDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog) {
    private val candyPriceAdapter by lazy { CandyPriceAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_recharge_candy_discount)
        initWindow()
        initView()
        giftRechargeList()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }


    private fun initView() {
        rvRechargeCandy.layoutManager = GridLayoutManager(myContext, 2)
        rvRechargeCandy.adapter = candyPriceAdapter

        candyPriceAdapter.setOnItemClickListener { _, view, position ->
            for (data in candyPriceAdapter.data) {
                candyPriceAdapter.data[position].checked = true
            }
            candyPriceAdapter.notifyDataSetChanged()
            ConfirmPayCandyDialog(myContext, candyPriceAdapter.data[position], payments).show()
        }
    }

    private val payments by lazy { mutableListOf<Paylist>() }
    fun giftRechargeList() {
        RetrofitFactory.instance.create(Api::class.java)
            .giftRechargeList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<RechargeBean?>>(null) {
                override fun onNext(t: BaseResp<RechargeBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        candyPriceAdapter.addData(t.data?.list ?: mutableListOf())
                        payments.addAll(t.data?.paylist ?: mutableListOf<Paylist>())
                    }
                }
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}