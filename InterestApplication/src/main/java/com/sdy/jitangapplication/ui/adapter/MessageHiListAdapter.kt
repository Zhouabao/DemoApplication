package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.HiMessageBean
import kotlinx.android.synthetic.main.item_message_hi_list.view.*
import kotlinx.android.synthetic.main.item_message_hi_list.view.latelyTime
import kotlinx.android.synthetic.main.item_message_hi_list.view.msgIcon
import kotlinx.android.synthetic.main.item_message_hi_list.view.msgTitle
import kotlinx.android.synthetic.main.item_message_hi_list.view.text
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   : 招呼列表adapter
 *    version: 1.0
 */
class MessageHiListAdapter :
    BaseQuickAdapter<HiMessageBean, BaseViewHolder>(R.layout.item_message_hi_list) {

    override fun convert(holder: BaseViewHolder, item: HiMessageBean) {
        val itemView = holder.itemView
        GlideUtil.loadAvatorImg(mContext, item.avatar, holder.itemView.msgIcon)
        holder.itemView.msgTitle.text = item.nickname
        holder.itemView.latelyTime.text = item.out_time
        holder.itemView.text.text = if (item.content.isEmpty()) {
            "对方向您打了一个招呼"
        } else {
            item.content
        }

        //"男.23岁.西安市"
        holder.itemView.msgUserInfo.text = "${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}.${item.age}岁.${item.distance}"


        holder.itemView.msgOnLineState.isVisible =
            NimUIKitImpl.enableOnlineState()
                    && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).isNullOrEmpty()
                    && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).contains("在线")


    }


}