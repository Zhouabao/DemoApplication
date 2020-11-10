package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.FootDescr
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.ui.adapter.FootPowerAdapter
import com.sdy.jitangapplication.ui.dialog.ConfirmPayCandyDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.ui.dialog.WhyPayDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_foot_price.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 门槛付费页面
 */
class FootPriceActivity : BaseActivity() {
    private val candyAmount by lazy { intent.getIntExtra("candy", 0) }

    private val adapter by lazy { FootPowerAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foot_price)
        EventBus.getDefault().register(this)
        initView()
        productLists()
    }

    private fun initView() {
        footNowPirce.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
        btnBack.clickWithTrigger {
            finish()
        }
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
        StatusBarUtil.immersive(this)

        val params = topBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (278F / 750 * params.width).toInt()
        topBg.layoutParams = params

        footPowerRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        footPowerRv.adapter = adapter
        adapter.addData(FootDescr(getString(R.string.pay_why1), R.drawable.icon_foot_price1))
        adapter.addData(FootDescr(getString(R.string.pay_why2), R.drawable.icon_foot_price2))
        adapter.addData(FootDescr(getString(R.string.pay_why3), R.drawable.icon_foot_price3))
        adapter.addData(FootDescr(getString(R.string.pay_why4), R.drawable.icon_foot_price4))


        //立即加入
        joinNowBtn.clickWithTrigger {
            ConfirmPayCandyDialog(this, chargeWayBeans[0], payways).show()
        }


        //为什么要付费
        whyPayBtn.clickWithTrigger {
            WhyPayDialog(this).show()
        }
    }

    /**
     * 请求支付方式
     */
    fun productLists() {
        RetrofitFactory.instance.create(Api::class.java)
            .getThreshold(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.code == 200) {
                        if (it.data != null) {
                            chargeWayBeans = it.data!!.list ?: mutableListOf()
                            setPurchaseType()
                            payways.addAll(it.data!!.paylist ?: mutableListOf())
                            footUserCount.text = getString(R.string.has, it.data!!.same_sex_cnt)

                            if (!chargeWayBeans.isNullOrEmpty() && chargeWayBeans[0].giving_amount > 0)
                                adapter.addData(
                                    FootDescr(
                                        getString(
                                            R.string.give,
                                            chargeWayBeans[0].giving_amount
                                        ), R.drawable.icon_foot_price5
                                    )
                                )

                        }
                    } else {
                        CommonFunction.toast(it.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(this@FootPriceActivity).show()
                    }
                }
            })
    }


    private var chargeWayBeans: MutableList<ChargeWayBean> = mutableListOf()
    private var payways: MutableList<PaywayBean> = mutableListOf()


    private fun setPurchaseType() {
        if (chargeWayBeans.isNotEmpty()) {
            SpanUtils.with(footOriginalPrice)
                .append(getString(R.string.original_price) + chargeWayBeans[0].original_price)
                .setFontSize(12, true)
                .setBold()
                .setStrikethrough()
                .create()

            SpanUtils.with(footNowPirce)
                .append("¥")
                .setFontSize(14, true)
                .append(
                    "${if (chargeWayBeans[0].type == 1) {
                        chargeWayBeans[0].original_price
                    } else {
                        chargeWayBeans[0].discount_price
                    }}"
                )
                .setFontSize(30, true)
                .setBold()
                .append(getString(R.string.forver))
                .setFontSize(14, true)
                .create()
        }
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