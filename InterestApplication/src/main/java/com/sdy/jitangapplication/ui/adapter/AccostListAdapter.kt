package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.nim.uikit.common.util.sys.TimeUtil
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class AccostListAdapter : BaseQuickAdapter<AccostBean, BaseViewHolder>(R.layout.item_message_list) {
    override fun convert(holder: BaseViewHolder, item: AccostBean) {
        holder.addOnClickListener(R.id.content)
        holder.addOnClickListener(R.id.menuDetele)
        if (holder.layoutPosition == data.size) {
            holder.itemView.msgDivider.visibility = View.INVISIBLE
        } else {
            holder.itemView.msgDivider.visibility = View.VISIBLE
        }

        holder.itemView.swipeLayout.isCanLeftSwipe = true
        holder.itemView.menuTop.isVisible = false
        holder.itemView.swipeLayout.isCanRightSwipe = false
        holder.itemView.newCount.text = "${item.unreadCnt}"
        holder.itemView.msgTitle.text = "${item.nickname}"
        GlideUtil.loadAvatorImg(
            mContext,
            item.avatar,
            holder.itemView.msgIcon
        )
        val recent = NIMClient.getService(MsgService::class.java)
            .queryRecentContact(item.accid, SessionTypeEnum.P2P)

        val message = NIMClient.getService(MsgService::class.java).queryLastMessage(item.accid, SessionTypeEnum.P2P)
        holder.itemView.text.text = when {
            recent != null -> {
                CommonFunction.getRecentContent(recent)
            }
            message != null -> {
                CommonFunction.getMessageContent(message)
            }
            item.content.isNotEmpty() -> {
                item.content
            }
            else -> {
                "新的招呼消息"
            }
        }

        holder.itemView.latelyTime.isVisible = item.time != 0L
        holder.itemView.latelyTime.text = TimeUtil.getTimeShowString(item.time, true)
        holder.itemView.msgOnLineState.isVisible = NimUIKitImpl.enableOnlineState()
                && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid)
            .isNullOrEmpty()
                && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid)
            .contains(
                "在线"
            )
        holder.itemView.newCount.isVisible = item.unreadCnt > 0

    }

}