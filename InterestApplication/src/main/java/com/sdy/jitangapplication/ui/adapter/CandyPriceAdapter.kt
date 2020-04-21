package com.sdy.jitangapplication.ui.adapter

import android.graphics.Typeface
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.RechargeCandyBean
import kotlinx.android.synthetic.main.item_candy_price.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 糖果价格表
 *    version: 1.0
 *
 */
class CandyPriceAdapter :
    BaseQuickAdapter<RechargeCandyBean, BaseViewHolder>(R.layout.item_candy_price) {

    override fun convert(holder: BaseViewHolder, item: RechargeCandyBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if ((holder.layoutPosition) % 2 == 0) {
            params.leftMargin = SizeUtils.dp2px(15F)
            params.rightMargin = SizeUtils.dp2px(11F)
        } else if ((holder.layoutPosition) % 2 == 1) {
            params.rightMargin = SizeUtils.dp2px(15F)
            params.leftMargin = 0
        }
        holder.itemView.layoutParams = params
        holder.itemView.candyPrice.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        holder.itemView.candyCount.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        holder.itemView.candyCount.text = "${item.amount}"
        holder.itemView.candyPrice.text = "¥${if (item.discount_price != 0F) {
            item.discount_price
        } else {
            item.original_price
        }}"
        holder.itemView.candyDiscount.isVisible = item.giving_amount > 0
        holder.itemView.candyDiscount.text = "赠送${item.giving_amount}"

        if (item.checked) {
            holder.itemView.rechargeCl.setBackgroundResource(R.drawable.shape_rectangle_orange_white_10dp)
        } else {
            holder.itemView.rechargeCl.setBackgroundResource(R.drawable.shape_rectangle_gray_10dp)
        }
    }

}