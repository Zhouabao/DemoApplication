package com.sdy.jitangapplication.ui.adapter

import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ChargeWayBean
import kotlinx.android.synthetic.main.item_charge_vip.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipChargeAdapter : BaseQuickAdapter<ChargeWayBean, BaseViewHolder>(R.layout.item_charge_vip) {
    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.height = SizeUtils.dp2px(130F)
        params.width =
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(17F) * 2 - SizeUtils.dp2px(9F) * 2) / 3F).toInt()
        if (holder.layoutPosition == 0) {
            params.leftMargin = SizeUtils.dp2px(17F)
            params.rightMargin = SizeUtils.dp2px(9F)
        } else if (holder.layoutPosition == mData.size - 1) {
            params.rightMargin = SizeUtils.dp2px(17F)
            params.leftMargin = SizeUtils.dp2px(9F)
        } else {
            params.rightMargin = SizeUtils.dp2px(0F)
            params.leftMargin = SizeUtils.dp2px(0F)
        }
        holder.itemView.layoutParams = params

        holder.itemView.vipNowPrice.text =
            SpanUtils.with(holder.itemView.vipNowPrice)
                .append("¥")
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
        holder.itemView.vipNowPrice.typeface = Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        holder.itemView.vipLong.typeface = Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        if (item.type == 1) {//	1 原价售卖 2折扣价售卖 3限时折扣
            holder.itemView.vipLong.visibility = View.INVISIBLE
        } else {
            holder.itemView.vipLong.visibility = View.VISIBLE
            holder.itemView.vipLong.text =
                SpanUtils.with(holder.itemView.vipLong).append("¥${item.original_price ?: "0"}")
                    .setStrikethrough().create()
        }
        holder.itemView.vipDiscount.text = item.ename ?: ""
        holder.itemView.vipSaleType.text = item.descr ?: ""
        if (item.check) {
            holder.itemView.vipSaleType.isVisible = true
//            holder.itemView.vipSaleType.isVisible = item.type == 3
            holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_vip_charge_checked_bg)
            holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
            holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
            holder.itemView.vipDiscount.setTextColor(mContext.resources.getColor(R.color.colorWhite))
            holder.itemView.vipDiscount.setBackgroundResource(R.drawable.shape_vip_charge_discount_checked_bg)

        } else {
            holder.itemView.vipSaleType.isVisible = false
            holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_vip_charge_normal_bg)
            holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipDiscount.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipDiscount.background = null


        }


        //                vipOneMonth.setBackgroundResource(R.drawable.shape_rectangle_orange)
//                vipThreeMonth.setBackgroundResource(R.drawable.shape_vip_charge_normal_bg)
//                vipOneYear.setBackgroundResource(R.drawable.shape_vip_charge_normal_bg)
    }
}