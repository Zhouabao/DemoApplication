package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.RefreshCandyMallDetailEvent
import com.sdy.jitangapplication.event.RefreshCandyMallEvent
import com.sdy.jitangapplication.event.RefreshMyCandyEvent
import com.sdy.jitangapplication.model.ExchangeOrderBean
import com.sdy.jitangapplication.ui.activity.CandyProductDetailActivity
import com.sdy.jitangapplication.ui.activity.MyCandyActivity
import com.sdy.jitangapplication.ui.activity.MyOrderActivity
import kotlinx.android.synthetic.main.dialog_exchange_success.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 兑换成功弹窗
 *    version: 1.0
 */
class ExchangeSuccessDialog(var context1: Context, val exchangeSuccessBean: ExchangeOrderBean) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_exchange_success)
        initWindow()
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(true)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }


    fun initview() {
        name.text = "${exchangeSuccessBean.receiver_name}\t${exchangeSuccessBean.phone}"
        address.text = exchangeSuccessBean.address
        GlideUtil.loadRoundImgCenterCrop(
            context1,
            exchangeSuccessBean.goods_icon,
            productIv,
            SizeUtils.dp2px(10F)
        )
        productDesc.text = exchangeSuccessBean.goods_title
        productCostCandy.text = SpanUtils.with(productCostCandy)
            .append("消耗糖果:\t\t")
            .appendImage(R.drawable.icon_candy_small)
            .append("${exchangeSuccessBean.goods_amount}")
            .setTypeface(Typeface.createFromAsset(context1.assets, "DIN_Alternate_Bold.ttf"))
            .create()
        oderNum.text = exchangeSuccessBean.goods_order
        orderTime.text = exchangeSuccessBean.create_time
        orderRemark.text = exchangeSuccessBean.order_remark

        confirmBtn.onClick {
            //兑换成功，刷新界面。
            // 我的糖果
            if (ActivityUtils.getTopActivity() is MyCandyActivity) {
                EventBus.getDefault().post(RefreshMyCandyEvent(-1))
            } else if (ActivityUtils.getTopActivity() is CandyProductDetailActivity) {
                //商品详情
                EventBus.getDefault().post(RefreshCandyMallDetailEvent())
            }
            //糖果商城、我的糖果
            EventBus.getDefault().post(RefreshCandyMallEvent())
            //更新个人中心糖果数量
            EventBus.getDefault().post(RefreshMyCandyEvent(exchangeSuccessBean.goods_amount))
            dismiss()
        }

        seeOrder.onClick {
            context1.startActivity<MyOrderActivity>()
            dismiss()
        }

    }

}