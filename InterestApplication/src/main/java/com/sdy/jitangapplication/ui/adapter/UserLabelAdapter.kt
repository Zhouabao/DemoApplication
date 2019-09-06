package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelBean
import kotlinx.android.synthetic.main.item_match_detail_label.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   : 个人中心标签适配器
 *    version: 1.0
 */
class UserLabelAdapter : BaseQuickAdapter<LabelBean, BaseViewHolder>(R.layout.item_match_detail_label) {
    override fun convert(holder: BaseViewHolder, model: LabelBean) {
        holder.itemView.isEnabled = false
        holder.itemView.labelName.text = model.title
        holder.itemView.labelName.isChecked = false
    }

}