package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
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
    var purchaseType = ChargeVipDialog.PURCHASE_VIP
    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        when (purchaseType) {
            ChargeVipDialog.PURCHASE_RENEW_VIP -> {
                params.width =
                    ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(37F) * 2 - SizeUtils.dp2px(9F) * 2) / 3F).toInt()
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
            }
            else -> {
                params.width =
                    ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(37F) * 2 - SizeUtils.dp2px(10F) * 2) / 3F).toInt()
                if (holder.layoutPosition == 0) {
                    params.leftMargin = SizeUtils.dp2px(17F)
                    params.rightMargin = SizeUtils.dp2px(10F)
                } else if (holder.layoutPosition == mData.size - 1) {
                    params.rightMargin = SizeUtils.dp2px(17F)
                    params.leftMargin = SizeUtils.dp2px(10F)
                } else {
                    params.rightMargin = SizeUtils.dp2px(0F)
                    params.leftMargin = SizeUtils.dp2px(0F)
                }
            }
        }
        holder.itemView.layoutParams = params




        holder.itemView.vipNowPrice.text =
            SpanUtils.with(holder.itemView.vipNowPrice)
                .append("¥")
                .setFontSize(14, true)
                .setBold()
                .append(
                    "${if (item.type == 1) {
                        BigDecimal(item.original_price).setScale(0)
                    } else {
                        BigDecimal(item.discount_price).setScale(0)
                    }}"
                )
                .setFontSize(28, true)
                .setBold()
                .create()
        holder.itemView.originalPrice.isVisible = item.type != 1 || item.giving_amount > 0
        if (item.giving_amount > 0) {
            holder.itemView.originalPrice.text =
                SpanUtils.with(holder.itemView.originalPrice)
                    .append("赠送${item.giving_amount}")
                    .appendImage(R.drawable.icon_candy_small)
                    .create()
        } else {
            holder.itemView.originalPrice.text =
                SpanUtils.with(holder.itemView.originalPrice)
                    .append("¥${item.original_price}")
                    .setStrikethrough()
                    .create()
        }



        holder.itemView.vipNowPrice.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")


        holder.itemView.vipLong.text = item.ename ?: ""
        holder.itemView.vipSaleType.text = item.descr ?: ""
        if (item.is_promote) {
            holder.itemView.vipDiscount.visibility = View.VISIBLE
            holder.itemView.vipDiscount.text = item.descr

//            holder.itemView.vipSaleType.isVisible = !item.descr.isNullOrEmpty()
            holder.itemView.vipSaleType.isVisible = false
//            holder.itemView.vipSaleType.isVisible = item.type == 3
            (holder.itemView.vipDiscount.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                SizeUtils.dp2px(8F)
            when (purchaseType) {
                ChargeVipDialog.PURCHASE_VIP -> {
                    holder.itemView.vipSaleType.setBackgroundResource(R.drawable.shape_vip_charge_popular_bg)
                    holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_vip_charge_checked_bg)
                    holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
                    holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
                    holder.itemView.vipDiscount.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.originalPrice.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
                    holder.itemView.vipSaleType.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.vipDiscount.setBackgroundResource(R.drawable.shape_vip_charge_discount_checked_bg)
                }
                ChargeVipDialog.PURCHASE_GREET_COUNT -> {
                    holder.itemView.vipSaleType.setBackgroundResource(R.drawable.shape_greet_charge_popular_bg)
                    holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_greet_charge_checked_bg)
                    holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorOrange))
                    holder.itemView.originalPrice.setTextColor(mContext.resources.getColor(R.color.colorOrange))
                    holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorOrange))
                    holder.itemView.vipDiscount.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.vipSaleType.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.vipDiscount.setBackgroundResource(R.drawable.shape_greet_charge_discount_checked_bg)
                }
                else -> {
                    holder.itemView.vipSaleType.setBackgroundResource(R.drawable.shape_vip_charge_renew_bg)
                    holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_vip_renew_charge_checked_bg)
                    holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
                    holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
                    holder.itemView.originalPrice.setTextColor(mContext.resources.getColor(R.color.colorOrangeVip))
                    holder.itemView.vipDiscount.setTextColor(Color.parseColor("#FF313437"))
                    holder.itemView.vipSaleType.setTextColor(Color.parseColor("#FF313437"))
                    holder.itemView.vipDiscount.setBackgroundResource(R.drawable.shape_vip_charge_discount_checked_bg)
                }
            }
        } else {
            holder.itemView.vipDiscount.visibility = View.GONE
            (holder.itemView.vipDiscount.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                SizeUtils.dp2px(0F)
            when (purchaseType) {
                ChargeVipDialog.PURCHASE_RENEW_VIP -> {
                    holder.itemView.vipSaleType.visibility = View.INVISIBLE
                    holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_vip_renew_charge_normal_bg)
                    holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.vipDiscount.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.originalPrice.setTextColor(mContext.resources.getColor(R.color.colorWhite))
                    holder.itemView.vipDiscount.background = null
                }
                else -> {
                    holder.itemView.vipSaleType.visibility = View.INVISIBLE
                    holder.itemView.vipCl.setBackgroundResource(R.drawable.shape_gray_7dp)
                    holder.itemView.vipLong.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
                    holder.itemView.vipNowPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
                    holder.itemView.vipDiscount.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
                    holder.itemView.originalPrice.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
                    holder.itemView.vipDiscount.background = null
                }
            }

        }

    }
}