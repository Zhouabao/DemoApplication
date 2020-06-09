package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AccostBean
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class AccostListAdapter : BaseQuickAdapter<AccostBean, BaseViewHolder>(R.layout.item_message_list) {
    override fun convert(holder: BaseViewHolder, item: AccostBean) {
        if (holder.layoutPosition == data.size) {
            holder.itemView.msgDivider.visibility = View.INVISIBLE
        } else {
            holder.itemView.msgDivider.visibility = View.VISIBLE
        }

        GlideUtil.loadAvatorImg(
            mContext,
            item.avatar,
            holder.itemView.msgIcon
        )


        holder.itemView.msgOnLineState.isVisible =
            NimUIKitImpl.enableOnlineState()
                    && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).isNullOrEmpty()
                    && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).contains(
                "在线"
            )


    }

}