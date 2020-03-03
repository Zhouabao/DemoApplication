package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.widget.LinearLayout
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_match_detail_label_quality.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   :
 *    version: 1.0
 */
class MatchDetailLabelQualityAdapter(var fromMatch: Boolean = true) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_match_detail_label_quality) {
    var myTags: MutableList<String> = mutableListOf()
    override fun convert(holder: BaseViewHolder, model: String) {
        holder.itemView.labelName.text = model
        if (fromMatch) {
            val params = holder.itemView.labelName.layoutParams as LinearLayout.LayoutParams
            params.height = SizeUtils.dp2px(26f)
            holder.itemView.labelName.layoutParams = params
            if (myTags.contains(model)) {
                holder.itemView.labelName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
                holder.itemView.setBackgroundResource(R.drawable.shape_rectangle_light_yellow_8dp)
            } else {
                holder.itemView.setBackgroundResource(R.drawable.shape_rectangle_lightgray_8dp)
                holder.itemView.labelName.setTextColor(Color.parseColor("#FF787C7F"))
            }
            holder.itemView.labelName.textSize = 13F
        } else {
            val params = holder.itemView.labelName.layoutParams as LinearLayout.LayoutParams
            params.height = SizeUtils.dp2px(18f)
            holder.itemView.labelName.layoutParams = params
            holder.itemView.labelName.textSize = 11F
            holder.itemView.labelName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.setBackgroundResource(R.drawable.shape_rectangle_light_yellow_4dp)
        }
    }
}