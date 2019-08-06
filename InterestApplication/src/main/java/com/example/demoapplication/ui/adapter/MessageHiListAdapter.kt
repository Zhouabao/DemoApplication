package com.example.demoapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.HiMessageBean
import kotlinx.android.synthetic.main.item_message_hi_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageHiListAdapter : BaseQuickAdapter<HiMessageBean, BaseViewHolder>(R.layout.item_message_hi_list) {
    override fun convert(holder: BaseViewHolder, item: HiMessageBean) {
        GlideUtil.loadAvatorImg(mContext, item.avatar, holder.itemView.msgIcon)
        holder.itemView.msgTitle.text = item.nickname ?: ""
        holder.itemView.msgText.text = item.content ?: ""
        holder.itemView.msgLatelyTime.text = item.create_time ?: ""
        holder.itemView.msgNewCount.text = "${item.count}"
        holder.itemView.msgText.text = "有${item.count}个人对你感兴趣，快来看看吧"
    }

}