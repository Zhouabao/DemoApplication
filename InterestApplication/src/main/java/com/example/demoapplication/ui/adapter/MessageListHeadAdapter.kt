package com.example.demoapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MessageListBean
import kotlinx.android.synthetic.main.item_message_list_top.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageListHeadAdapter : BaseQuickAdapter<MessageListBean, BaseViewHolder>(R.layout.item_message_list_top) {
    override fun convert(holder: BaseViewHolder, item: MessageListBean) {
        if (item.icon != null)
            holder.itemView.icon.setImageResource(item.icon)
        holder.itemView.title.text = item.title
        holder.itemView.text.text = item.msg
        holder.itemView.latelyTime.text = item.time
        if (item.count == null || item.count == 0) {
            holder.itemView.newCount.visibility = View.GONE
        } else {
            holder.itemView.newCount.visibility = View.VISIBLE
            holder.itemView.newCount.text = "${item.count}"
        }
    }

}