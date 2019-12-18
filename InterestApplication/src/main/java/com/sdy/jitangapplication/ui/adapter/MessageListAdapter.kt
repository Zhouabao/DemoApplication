package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.util.sys.TimeUtil
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageListAdapter : BaseQuickAdapter<RecentContact, BaseViewHolder>(R.layout.item_message_list) {
    override fun convert(holder: BaseViewHolder, item: RecentContact) {
        holder.addOnClickListener(R.id.menuTop)
        holder.addOnClickListener(R.id.menuDetele)
        holder.addOnClickListener(R.id.content)
        if (holder.layoutPosition == data.size) {
            holder.itemView.msgDivider.visibility = View.INVISIBLE
        } else {
            holder.itemView.msgDivider.visibility = View.VISIBLE
        }

        if (item.contactId != null) {
            holder.itemView.msgTitle.text = UserInfoHelper.getUserDisplayName(item.contactId)
        }
        GlideUtil.loadAvatorImg(mContext, UserInfoHelper.getAvatar(item.contactId), holder.itemView.msgIcon)



        if (item.attachment is ChatHiAttachment) {
            holder.itemView.text.text =
                if ((item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                    "『招呼消息』"
                } else if ((item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH) {
                    "『匹配消息』"
                } else if ((item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND) {
                    "『好友消息』"
                } else if ((item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME) {
                    "『消息过期』"
                } else {
                    ""
                }

        } else if (item.attachment is ShareSquareAttachment) {
            holder.itemView.text.text = "『动态分享内容』"
        } else {
            holder.itemView.text.text = item.content
        }
        holder.itemView.latelyTime.text = TimeUtil.getTimeShowString(item.time, true)
        if (item.unreadCount == 0) {
            holder.itemView.newCount.visibility = View.GONE
        } else {
            holder.itemView.newCount.text = "${item.unreadCount}"
            holder.itemView.newCount.visibility = View.VISIBLE
        }
    }

}