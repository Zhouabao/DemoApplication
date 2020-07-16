package com.sdy.jitangapplication.ui.adapter

import android.graphics.Typeface
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ChargeWayBean
import kotlinx.android.synthetic.main.item_charge_vip.view.*
import java.math.BigDecimal

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipChargeAdapter() :
    BaseQuickAdapter<ChargeWayBean, BaseViewHolder>(R.layout.item_charge_vip) {
    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {

        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.leftMargin = if (holder.layoutPosition == 0) {
            SizeUtils.dp2px(13F)
        } else {
            0
        }
        params.rightMargin = if (holder.layoutPosition == mData.size - 1) {
            SizeUtils.dp2px(13F)
        } else {
            0
        }
        holder.itemView.layoutParams = params

        holder.itemView.vipNowPrice.text =
            SpanUtils.with(holder.itemView.vipNowPrice)
                .append("¥")
                .setFontSize(14, true)
                .setBold()
                .append(
                    "${if (item.type == 1) {
                        BigDecimal(item.original_price).setScale(0, BigDecimal.ROUND_HALF_UP)
                    } else {
                        BigDecimal(item.discount_price).setScale(0, BigDecimal.ROUND_HALF_UP)
                    }}"
                )
                .setFontSize(30, true)
                .setBold()
                .create()
        if (item.giving_amount > 0) {
            holder.itemView.vipSendCandy.text = "赠送${item.giving_amount}糖果"
            holder.itemView.vipSendCandy.isVisible = true
        } else {
            holder.itemView.vipSendCandy.isVisible = false
        }

        holder.itemView.vipNowPrice.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")

        holder.itemView.vipLong.text = item.ename ?: ""

        //todo ≈好多钱每天
        holder.itemView.vipEachDayPrice.text = item.ename ?: ""

        if (item.is_promote) {
            holder.itemView.purchaseCl.setBackgroundResource(R.drawable.shape_rectangle_blue_stroke_10dp)
            holder.itemView.vipSendCandy.setBackgroundResource(R.drawable.shape_rectangle_dark_blue_bottom_10_corners_bg)
        } else {
            holder.itemView.purchaseCl.setBackgroundResource(R.drawable.rectangle_white_10dp)
            holder.itemView.vipSendCandy.setBackgroundResource(R.drawable.shape_rectangle_light_blue_bottom_10_corners_bg)
        }

    }
}