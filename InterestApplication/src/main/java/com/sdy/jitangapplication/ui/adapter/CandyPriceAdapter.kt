package com.sdy.jitangapplication.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ChargeWayBean
import kotlinx.android.synthetic.main.item_candy_price.view.*
import java.math.BigDecimal

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 糖果价格表
 *    version: 1.0
 *
 */
class CandyPriceAdapter :
    BaseQuickAdapter<ChargeWayBean, BaseViewHolder>(R.layout.item_candy_price) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {
        holder.itemView.candyCount.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        holder.itemView.candyCount.text = "${item.amount}"
        //isfirst
        //不是首冲显示原价
        if (item.isfirst) {
            SpanUtils.with(holder.itemView.candyFirstPrice)
                .append(
                    "${CommonFunction.getNowMoneyUnit()}${BigDecimal(item.discount_price).setScale(
                        0,
                        BigDecimal.ROUND_HALF_UP
                    )}"
                )
                .setTypeface(Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf"))
                .create()

            SpanUtils.with(holder.itemView.candyPrice)
                .append(
                    "${mContext.getString(R.string.original_price)}${BigDecimal(item.original_price).setScale(
                        0,
                        BigDecimal.ROUND_HALF_UP
                    )},"
                )
                .setStrikethrough()
                .append(mContext.getString(R.string.first_charge_save))
                .append(
                    "${BigDecimal(item.original_price - item.discount_price).setScale(
                        0,
                        BigDecimal.ROUND_HALF_UP
                    )}"
                )
                .setForegroundColor(mContext.resources.getColor(R.color.colorOrange))
                .create()

        } else {
            SpanUtils.with(holder.itemView.candyFirstPrice)
                .append(
                    "${CommonFunction.getNowMoneyUnit()}${BigDecimal(
                        if (item.discount_price != 0.0) {
                            item.discount_price
                        } else {
                            item.original_price
                        }
                    ).setScale(
                        0,
                        BigDecimal.ROUND_HALF_UP
                    )}"
                )
                .setTypeface(Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf"))
                .create()

            holder.itemView.candyPrice.text = mContext.getString(R.string.cost) + BigDecimal(
                if (item.discount_price != 0.0) {
                    item.discount_price
                } else {
                    item.original_price
                }
            ).setScale(
                0,
                BigDecimal.ROUND_HALF_UP
            ) + mContext.getString(R.string.money_unit)

        }

        holder.itemView.candyDiscount.isVisible = item.isfirst
    }

}