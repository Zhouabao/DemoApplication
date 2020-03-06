package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.item_label_publish_index.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/249:54
 *    desc   : 发布页面兴趣的adapter  兴趣点击进行删除
 *    version: 1.0
 */
class PublishLabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label_publish_index) {
    override fun convert(holder: BaseViewHolder, item: NewLabel) {
        holder.itemView.tagName.text = item.title
        GlideUtil.loadCircleImg(mContext, item.icon, holder.itemView.tagIcon)
    }
}