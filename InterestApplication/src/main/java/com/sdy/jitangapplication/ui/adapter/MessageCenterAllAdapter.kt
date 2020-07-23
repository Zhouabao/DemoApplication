package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
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
        if (NIMClient.getService(MsgService::class.java)
                .queryRecentContact(item.accid, SessionTypeEnum.P2P) != null
            && NIMClient.getService(MsgService::class.java)
                .queryRecentContact(item.accid, SessionTypeEnum.P2P).unreadCount > 0
        ) {
            holder.itemView.accostGiftIv.visibility = View.VISIBLE
        } else
            holder.itemView.accostGiftIv.visibility = View.INVISIBLE

    }

}