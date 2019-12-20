package com.sdy.jitangapplication.ui.adapter

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
class MatchDetailLabelQualityAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_match_detail_label_quality) {
    var myTags: MutableList<String> = mutableListOf()
    override fun convert(holder: BaseViewHolder, model: String) {
        holder.itemView.labelName.text = model
        if (myTags.contains(model)) {
            holder.itemView.labelName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
        } else {
            holder.itemView.labelName.setTextColor(mContext.resources.getColor(R.color.colorGrayE0))
        }
    }
}