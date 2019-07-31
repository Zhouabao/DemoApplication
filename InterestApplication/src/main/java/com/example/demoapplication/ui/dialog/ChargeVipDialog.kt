package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.ChargeWayBean
import com.example.demoapplication.model.ChargeWayBeans
import com.example.demoapplication.model.PaywayBean
import com.example.demoapplication.model.VipDescr
import com.example.demoapplication.ui.adapter.VipBannerAdapter
import com.example.demoapplication.ui.adapter.VipChargeAdapter
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.DividerItemDecoration
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import kotlinx.android.synthetic.main.dialog_charge_vip.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 充值会员底部对话框
 *    version: 1.0
 */
class ChargeVipDialog(context: Context) : Dialog(context, R.style.MyDialog) {
    private var payways: MutableList<PaywayBean> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_charge_vip)
        initWindow()
        initView()
        initChargeWay()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }


    private val vipChargeAdapter by lazy { VipChargeAdapter() }
    /**
     * 设置支付价格的数据
     */
    private fun setChargeWayData(chargeWays: MutableList<ChargeWayBean>) {
        vipChargeAdapter.setNewData(chargeWays)
    }


    /**
     * 设置VIP的权益广告栏
     */
    private val vipBannerAdapter by lazy { VipBannerAdapter() }
    public var position: Int = 0
    public fun setCurrent(position: Int) {
        bannerVip.setCurrentItem(position, true)
    }

    //权益栏
    private fun initVipPowerData(banners: MutableList<VipDescr>) {
        bannerVip.adapter = vipBannerAdapter
        vipBannerAdapter.setNewData(banners)
        if (vipBannerAdapter.data.size > 0) {
            val size = vipBannerAdapter.data.size
            for (i in 0 until size) {
                val indicator = RadioButton(context)
                indicator.width = SizeUtils.dp2px(5F)
                indicator.height = SizeUtils.dp2px(5F)
                indicator.buttonDrawable = null
                indicator.background = context.resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                indicator.layoutParams = layoutParams
                indicator.isEnabled = false
                indicator.isChecked = i == 0
                bannerIndicator.addView(indicator)
            }
        }
        bannerVip.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (child in 0 until bannerIndicator.childCount)
                    (bannerIndicator.getChildAt(child) as RadioButton).isChecked = position == child
            }
        })
        bannerVip.setCurrentItem(position, true)
    }


    private fun initView() {
        //支付价格
        vipChargeRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        vipChargeRv.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(8F),
                context.resources.getColor(R.color.colorWhite)
            )
        )
        vipChargeRv.adapter = vipChargeAdapter
        vipChargeAdapter.setOnItemClickListener { _, _, position ->
            for (data in vipChargeAdapter.data.withIndex()) {
                data.value.check = data.index == position
            }
            vipChargeAdapter.notifyDataSetChanged()
            ToastUtils.showShort("${position}")
        }
    }

    /**
     * 请求支付方式
     */
    private fun initChargeWay() {
        RetrofitFactory.instance.create(Api::class.java)
            .productLists(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "_sign" to "",
                    "_timestamp" to System.currentTimeMillis()
                )
            )
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.data != null) {
                        it.data!!.list?.get(0)?.check = true
                        setChargeWayData(it.data!!.list ?: mutableListOf())
                        initVipPowerData(it.data!!.icon_list ?: mutableListOf())
                        payways.addAll(it.data!!.paylist ?: mutableListOf())
                        initPayWay()
                        loading.visibility = View.GONE
                        dialogView.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun initPayWay() {
        for (payway in payways) {
            if (payway.id == 1) {
                zhiPayBtn.visibility = View.VISIBLE
            }
            if (payway.id == 2) {
                wechatPayBtn.visibility = View.VISIBLE
            }
            if (payway.id == 3) {
                balancePayBtn.visibility = View.VISIBLE
            }
        }
    }


    /**
     * 支付
     */
    private fun chargeVip() {
        setCancelable(false)
        setCanceledOnTouchOutside(false)

    }
}