package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.business.recent.RecentContactsFragment
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.CommonUtil
import com.netease.nim.uikit.common.util.sys.TimeUtil
import com.netease.nim.uikit.impl.NimUIKitImpl
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

        if (CommonUtil.isTagSet(item, RecentContactsFragment.RECENT_TAG_STICKY)) {
            holder.itemView.menuTop.setImageResource(R.drawable.icon_top_cancel_msg)
        } else {
            holder.itemView.menuTop.setImageResource(R.drawable.icon_top_msg)
        }

        when {
            item.attachment is ChatHiAttachment -> holder.itemView.text.text =
                when {
                    (item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI -> "『招呼消息』"
                    (item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH -> "『匹配消息』"
                    (item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND -> "『好友消息』"
                    (item.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME -> "『消息过期』"
                    else -> ""
                }
            item.attachment is ShareSquareAttachment -> holder.itemView.text.text = "『动态分享内容』"
            else -> holder.itemView.text.text = item.content
        }
        holder.itemView.latelyTime.text = TimeUtil.getTimeShowString(item.time, true)
        if (item.unreadCount == 0) {
            holder.itemView.newCount.visibility = View.GONE
        } else {
            holder.itemView.newCount.text = "${item.unreadCount}"
            holder.itemView.newCount.visibility = View.VISIBLE
        }

        holder.itemView.msgOnLineState.isVisible =
            NimUIKitImpl.enableOnlineState()
                    && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.contactId).isNullOrEmpty()
                    && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.contactId).contains("在线")


    }

}