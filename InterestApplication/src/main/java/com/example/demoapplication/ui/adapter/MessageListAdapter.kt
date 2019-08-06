package com.example.demoapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MessageListBean
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageListAdapter : BaseQuickAdapter<MessageListBean, BaseViewHolder>(R.layout.item_message_list) {
    override fun convert(holder: BaseViewHolder, item: MessageListBean) {
        holder.addOnClickListener(R.id.menuTop)
        holder.addOnClickListener(R.id.menuDetele)
        holder.addOnClickListener(R.id.content)
        if (holder.layoutPosition == data.size - 1) {
            holder.itemView.msgDivider.visibility = View.INVISIBLE
        } else {
            holder.itemView.msgDivider.visibility = View.VISIBLE
        }

        holder.itemView.msgIcon.setImageResource(
            if (holder.layoutPosition % 2 == 0) {
                R.drawable.img_avatar_01
            } else {
                R.drawable.icon_default_avator
            }
        )
        holder.itemView.msgTitle.text = item.title
        holder.itemView.msgText.text = item.msg
        holder.itemView.msgLatelyTime.text = item.time
        holder.itemView.msgNewCount.text = "${item.count}"
    }

}