package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_match_detail_label.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   :
 *    version: 1.0
 */
class MatchDetailLabelQualityAdapter :
    BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_match_detail_label_quality) {
    override fun convert(holder: BaseViewHolder, model: LabelQualityBean) {
        holder.itemView.isEnabled = false
        holder.itemView.labelName.text = model.content
        holder.itemView.labelName.isChecked = model.checked
    }
}