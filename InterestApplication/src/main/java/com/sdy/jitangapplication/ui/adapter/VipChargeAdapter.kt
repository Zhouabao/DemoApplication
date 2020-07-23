package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.ui.dialog.ChargePtVipDialog
import kotlinx.android.synthetic.main.item_charge_vip.view.*
import java.math.BigDecimal

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipChargeAdapter(val type: Int = ChargePtVipDialog.PURCHASE_VIP) :
    BaseQuickAdapter<ChargeWayBean, BaseViewHolder>(R.layout.item_charge_vip) {
    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.leftMargin = SizeUtils.dp2px(15F)
        params.rightMargin = if (holder.layoutPosition == mData.size - 1) {
            SizeUtils.dp2px(13F)
        } else {
            0
        }
        holder.itemView.layoutParams = params

        holder.itemView.vipLong.text = item.ename ?: ""
        holder.itemView.monthPrice.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        holder.itemView.monthPrice.text =
            SpanUtils.with(holder.itemView.monthPrice)
                .append(item.unit_price)
                .setFontSize(30, true)
                .setBold()
                .append("/月")
                .setFontSize(14, true)
                .setBold()
                .create()

        holder.itemView.vipNowPrice.text = "¥${if (item.type == 1) {
            BigDecimal(item.original_price).setScale(0, BigDecimal.ROUND_HALF_UP)
        } else {
            BigDecimal(item.discount_price).setScale(0, BigDecimal.ROUND_HALF_UP)
        }}"

        if (item.giving_amount > 0) {
            holder.itemView.vipSendCandy.text = "赠送${item.giving_amount}糖果"
            holder.itemView.vipSendCandy.isVisible = true
        } else {
            holder.itemView.vipSendCandy.isVisible = false
        }


        if (type == ChargePtVipDialog.PURCHASE_VIP) {
            if (item.is_promote) {
                holder.itemView.vipLong.setTextColor(Color.parseColor("#FFF2B769"))
                holder.itemView.monthPrice.setTextColor(Color.parseColor("#FFF2B769"))
                holder.itemView.vipNowPrice.setTextColor(Color.parseColor("#FFF2B769"))
                holder.itemView.vipSendCandy.setTextColor(Color.WHITE)
                holder.itemView.purchaseCl.setBackgroundResource(R.drawable.shape_rectangle_dark_blue_stroke_10dp)
                holder.itemView.vipSendCandy.setBackgroundResource(R.drawable.shape_rectangle_dark_blue_bottom_10_corners_bg)
            } else {
                holder.itemView.vipLong.setTextColor(Color.parseColor("#FFB0B2B6"))
                holder.itemView.monthPrice.setTextColor(Color.parseColor("#FF666666"))
                holder.itemView.vipNowPrice.setTextColor(Color.parseColor("#FFB0B2B6"))
                holder.itemView.vipSendCandy.setTextColor(Color.parseColor("#FF666666"))
                holder.itemView.purchaseCl.setBackgroundResource(R.drawable.shape_rectangle_grayf9_10dp)
                holder.itemView.vipSendCandy.setBackgroundResource(R.drawable.shape_rectangle_light_gray_bottom_10_corners_bg)
            }
        } else {
            holder.itemView.vipSendCandy.isVisible = false
            if (item.is_promote) {
                holder.itemView.vipLong.setTextColor(Color.parseColor("#FFF2B769"))
                holder.itemView.monthPrice.setTextColor(Color.parseColor("#FFF2B769"))
                holder.itemView.vipNowPrice.setTextColor(Color.parseColor("#FFF2B769"))
                holder.itemView.vipSendCandy.setTextColor(Color.WHITE)
                holder.itemView.purchaseCl.setBackgroundResource(R.drawable.shape_rectangle_dark_black_10dp)
            } else {
                holder.itemView.vipLong.setTextColor(Color.parseColor("#FFB0B2B6"))
                holder.itemView.monthPrice.setTextColor(Color.parseColor("#FF666666"))
                holder.itemView.vipNowPrice.setTextColor(Color.parseColor("#FFB0B2B6"))
                holder.itemView.vipSendCandy.setTextColor(Color.parseColor("#FF666666"))
                holder.itemView.purchaseCl.setBackgroundResource(R.drawable.shape_rectangle_grayf9_10dp)
            }
        }
    }
}