package com.example.demoapplication.ui.adapter

import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.ChargeWayBean
import kotlinx.android.synthetic.main.item_charge_vip.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipChargeAdapter : BaseQuickAdapter<ChargeWayBean, BaseViewHolder>(R.layout.item_charge_vip) {
    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {
        holder.itemView.vipNowPrice.text =
            SpanUtils.with(holder.itemView.vipNowPrice)
                .append("￥")
                .setFontSize(14, true)
                .setBold()
                .append(
                    if (item.type == 1) {
                        "${item.original_price ?: ""}"
                    } else {
                        "${item.discount_price ?: ""}"
                    }
                )
                .setFontSize(28, true)
                .setBold()
                .create()
        if (item.type == 1) {
            holder.itemView.vipOriginalPrice.visibility = View.INVISIBLE
        } else {
            holder.itemView.vipOriginalPrice.visibility = View.VISIBLE
            holder.itemView.vipOriginalPrice.text =
                SpanUtils.with(holder.itemView.vipOriginalPrice).append("￥${item.discount_price ?: "0"}")
                    .setStrikethrough().create()
        }
        holder.itemView.vipLong.text = item.ename ?: ""
        holder.itemView.vipSaleType.text = item.title ?: ""
        if (item.check) {
            holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_rectangle_orange)
            holder.itemView.vipSaleType.visibility = View.VISIBLE
            holder.itemView.vipOriginalPrice.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorOrange))

        } else {
            holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
            holder.itemView.vipSaleType.visibility = View.GONE
            holder.itemView.vipOriginalPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))

        }


        //                vipOneMonth.setBackgroundResource(R.drawable.shape_rectangle_orange)
//                vipThreeMonth.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
//                vipOneYear.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
    }
}