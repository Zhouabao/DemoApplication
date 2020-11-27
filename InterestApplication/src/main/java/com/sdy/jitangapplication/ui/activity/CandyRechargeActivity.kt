package com.sdy.jitangapplication.ui.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.PayPalResultEvent
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.presenter.ChargeVipPresenter
import com.sdy.jitangapplication.presenter.view.ChargeVipView
import com.sdy.jitangapplication.ui.adapter.CandyPriceAdapter
import kotlinx.android.synthetic.main.activity_charge_vip.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 糖果充值页面
 */
class CandyRechargeActivity : BaseMvpActivity<ChargeVipPresenter>(), ChargeVipView {
    private val candyPriceAdapter by lazy { CandyPriceAdapter() }
    private val payments by lazy { mutableListOf<PaywayBean>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge_vip)
        initView()
        mPresenter.giftRechargeList()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = ChargeVipPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = getString(R.string.candy_charge)
        btnBack.clickWithTrigger {
            finish()
        }

        candyAmount.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")


        vipChargeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        vipChargeRv.adapter = candyPriceAdapter
        candyPriceAdapter.setOnItemClickListener { _, view, position ->
           CommonFunction.startToPay(this, candyPriceAdapter.data[position], payments)
        }
    }

    override fun giftRechargeListResult(data: ChargeWayBeans?) {
        if (data != null) {
            if (!data?.list.isNullOrEmpty()) {
                data!!.list!![0].checked = true
            }
            candyAmount.text = "${data?.mycandy_amount}"
            firstChargeTime.text = data?.first_recharge ?: ""
            candyPriceAdapter.addData(data?.list ?: mutableListOf())
            payments.addAll(data?.paylist ?: mutableListOf<PaywayBean>())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EventBus.getDefault().post(PayPalResultEvent(requestCode, resultCode, data))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        finish()
    }

}