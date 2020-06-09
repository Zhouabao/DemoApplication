package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AccostBean
import kotlinx.android.synthetic.main.item_message_center_all.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageCenterAllAdapter :
    BaseQuickAdapter<AccostBean, BaseViewHolder>(R.layout.item_message_center_all) {
    override fun convert(holder: BaseViewHolder, item: AccostBean) {
        GlideUtil.loadCircleImg(
            mContext,
            item.avatar,
            holder.itemView.accostUserIv
        )
        GlideUtil.loadImg(
            mContext,
            item.icon,
            holder.itemView.accostGiftIv
        )
    }

}