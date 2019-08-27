package com.example.demoapplication.ui.adapter

import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
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
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.height = SizeUtils.dp2px(130F)
        params.width =
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(41F) * 2 - SizeUtils.dp2px(9F) * 2) / 3F).toInt()
        if (holder.layoutPosition == 0) {
            params.leftMargin = SizeUtils.dp2px(26F)
            params.rightMargin = SizeUtils.dp2px(9F)
        } else if (holder.layoutPosition == mData.size - 1) {
            params.rightMargin = SizeUtils.dp2px(26F)
            params.leftMargin = SizeUtils.dp2px(9F)
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
        holder.itemView.vipOriginalPrice.typeface = Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        if (item.type == 1) {//	1 原价售卖 2折扣价售卖 3限时折扣
            holder.itemView.vipOriginalPrice.visibility = View.INVISIBLE
        } else {
            holder.itemView.vipOriginalPrice.visibility = View.VISIBLE
            holder.itemView.vipOriginalPrice.text =
                SpanUtils.with(holder.itemView.vipOriginalPrice).append("¥${item.discount_price ?: "0"}")
                    .setStrikethrough().create()
        }
        holder.itemView.vipLong.text = item.ename ?: ""
        holder.itemView.vipSaleType.text = item.descr ?: ""
        if (item.check) {
            holder.itemView.vipSaleType.isVisible = item.type == 3
            holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_rectangle_orange)
            holder.itemView.vipOriginalPrice.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorOrange))

        } else {
            holder.itemView.vipSaleType.isVisible = false
            holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
            holder.itemView.vipOriginalPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))

        }


        //                vipOneMonth.setBackgroundResource(R.drawable.shape_rectangle_orange)
//                vipThreeMonth.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
//                vipOneYear.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
    }
}