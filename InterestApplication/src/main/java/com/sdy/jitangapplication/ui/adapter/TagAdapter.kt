package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.TagBean
import kotlinx.android.synthetic.main.item_match_tag.view.*

class TagAdapter : BaseQuickAdapter<TagBean, BaseViewHolder>(R.layout.item_match_tag) {
    override fun convert(helper: BaseViewHolder, item: TagBean) {
        helper.itemView.tagName.text = item.title
        if (item.cheked == true) {
            helper.itemView.tagName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
        } else {
            helper.itemView.tagName.setTextColor(mContext.resources.getColor(R.color.colorBlack8D))
        }

        if (helper.layoutPosition == mData.size - 1) {
            helper.itemView.tagName.isVisible = false
            helper.itemView.addTag.isVisible = true
        } else {
            helper.itemView.tagName.isVisible = true
            helper.itemView.addTag.isVisible = false
        }
    }
}