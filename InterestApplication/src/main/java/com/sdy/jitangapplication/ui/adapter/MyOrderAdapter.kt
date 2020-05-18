package com.sdy.jitangapplication.ui.adapter

import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyOrderBean
import kotlinx.android.synthetic.main.item_my_order.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2517:43
 *    desc   :想要商品
 *    version: 1.0
 */
class MyOrderAdapter : BaseQuickAdapter<MyOrderBean, BaseViewHolder>(R.layout.item_my_order) {
    override fun convert(helper: BaseViewHolder, item: MyOrderBean) {
        helper.addOnClickListener(R.id.orderState)
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.orderImg,
            SizeUtils.dp2px(10F)
        )
        helper.itemView.orderContent.text = item.title
        helper.itemView.orderCost.text = SpanUtils.with(helper.itemView.orderCost)
            .append("消耗\t")
            .appendImage(R.drawable.icon_candy_small)
            .append("\t${item.amount}")
            .create()
        //状态 1等待发货 2已经退货 3确认收货 4已收货、
        when (item.state) {
            1 -> {
                helper.itemView.orderState.text = "等待发货"
                helper.itemView.orderState.isEnabled = false
            }
            3 -> {
                helper.itemView.orderState.text = "确认收货"
                helper.itemView.orderState.isEnabled = true
            }
            4 -> {
                helper.itemView.orderState.text = "已收货"
                helper.itemView.orderState.isEnabled = false
            }
            else -> {
                helper.itemView.orderState.text = "已经退货"
                helper.itemView.orderState.isEnabled = false
            }
        }


    }
}