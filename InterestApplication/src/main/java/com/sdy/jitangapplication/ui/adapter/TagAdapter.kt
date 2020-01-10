package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.TagBean
import kotlinx.android.synthetic.main.item_match_tag.view.*

class TagAdapter : BaseQuickAdapter<TagBean, BaseViewHolder>(R.layout.item_match_tag) {
    override fun convert(helper: BaseViewHolder, item: TagBean) {
        helper.itemView.tagName.text = item.title
        (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = if (helper.layoutPosition == 0) {
            SizeUtils.dp2px(15F)
        } else {
            0
        }
        if (item.cheked == true) {
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_gray_divider_14dp)
            helper.itemView.tagName.setTextColor(mContext.resources.getColor(R.color.colorBlack19))
            helper.itemView.tagName.paint.isFakeBoldText = true
        } else {
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_white_14dp)
            helper.itemView.tagName.setTextColor(mContext.resources.getColor(R.color.colorBlack8D))
            helper.itemView.tagName.paint.isFakeBoldText = false
        }

        helper.itemView.tagName.isVisible = true
    }
}