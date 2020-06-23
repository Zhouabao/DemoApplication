package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
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

    override fun convert(holder: BaseViewHolder, item: ChargeWayBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if ((holder.layoutPosition) % 2 == 0) {
            params.leftMargin = SizeUtils.dp2px(15F)
            params.rightMargin = SizeUtils.dp2px(11F)
        } else if ((holder.layoutPosition) % 2 == 1) {
            params.rightMargin = SizeUtils.dp2px(15F)
            params.leftMargin = 0
        }
        holder.itemView.layoutParams = params
        holder.itemView.candyCount.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        holder.itemView.candyCount.text = "${item.amount}"

        //isfirst
        //不是首冲显示原价
        if (item.isfirst) {
            SpanUtils.with(holder.itemView.candyFirstPrice)
                .append("首充 ")
                .setBold()
                .append("¥${item.discount_price}")
                .setTypeface(Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf"))
                .create()

            holder.itemView.candyPrice.visibility = View.VISIBLE
            SpanUtils.with(holder.itemView.candyPrice)
                .append("原价${BigDecimal(item.original_price).setScale(0, BigDecimal.ROUND_HALF_UP)}")
                .setStrikethrough()
                .append(",首充立减${BigDecimal(item.original_price - item.discount_price).setScale(0, BigDecimal.ROUND_HALF_UP)}")
                .create()

        } else {
            SpanUtils.with(holder.itemView.candyFirstPrice)
                .append("价格 ")
                .setBold()
                .append(
                    "¥${if (item.discount_price != 0.0) {
                        item.discount_price
                    } else {
                        item.original_price
                    }}"
                )
                .setTypeface(Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf"))
                .create()

            holder.itemView.candyPrice.visibility = View.INVISIBLE
        }




        holder.itemView.candyDiscount.isVisible = item.isfirst
        holder.itemView.candyDiscount.text = "首冲折扣"

        if (item.checked) {
            holder.itemView.rechargeCl.setBackgroundResource(R.drawable.shape_rectangle_orange_white_10dp)
            holder.itemView.candyCount.setTextColor(Color.parseColor("#ff6318"))
        } else {
            holder.itemView.candyCount.setTextColor(Color.parseColor("#191919"))
            holder.itemView.rechargeCl.setBackgroundResource(R.drawable.shape_rectangle_gray_10dp)
        }
    }

}