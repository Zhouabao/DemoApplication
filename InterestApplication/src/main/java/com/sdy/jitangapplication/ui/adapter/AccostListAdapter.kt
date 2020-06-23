package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.utils.UserManager
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
        if (holder.layoutPosition == data.size) {
            holder.itemView.msgDivider.visibility = View.INVISIBLE
        } else {
            holder.itemView.msgDivider.visibility = View.VISIBLE
        }

        holder.itemView.swipeLayout.isCanLeftSwipe = false
        holder.itemView.swipeLayout.isCanRightSwipe = false
        holder.itemView.newCount.text = "${item.unreadCnt}"
        GlideUtil.loadAvatorImg(
            mContext,
            item.avatar,
            holder.itemView.msgIcon
        )


        holder.itemView.msgOnLineState.isVisible = NimUIKitImpl.enableOnlineState()
                && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).isNullOrEmpty()
                && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).contains(
            "在线"
        )
        holder.itemView.newCount.isVisible = item.unreadCnt > 0
        if (UserManager.getGender() == 1) {
            holder.itemView.text.text = "新的搭讪消息"
        } else {
            if (item.unreadCnt > 0) {
                SpanUtils.with(holder.itemView.text)
                    .append("[礼物]")
                    .setForegroundColor(Color.parseColor("#FFFD4417"))
                    .append("搭讪礼物待领取")
                    .setForegroundColor(Color.parseColor("#FFCCCDCF"))
                    .create()
            } else {
                holder.itemView.text.text = "[礼物]搭讪礼物待领取"
            }
        }
    }

}