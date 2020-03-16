package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.TopicBean
import kotlinx.android.synthetic.main.item_square_title.view.*

/**
 * 选择话题
 */
class SquareTitleAdapter : BaseQuickAdapter<TopicBean, BaseViewHolder>(R.layout.item_square_title) {
    override fun convert(helper: BaseViewHolder, item: TopicBean) {

        helper.itemView.squareTitle.text = item.title
    }
}