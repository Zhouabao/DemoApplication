package com.sdy.jitangapplication.ui.adapter

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MessageGiftBean
import com.sdy.jitangapplication.nim.uikit.api.NimUIKit
import com.sdy.jitangapplication.nim.uikit.business.recent.RecentContactsFragment
import com.sdy.jitangapplication.nim.uikit.business.uinfo.UserInfoHelper
import com.sdy.jitangapplication.nim.uikit.common.CommonUtil
import com.sdy.jitangapplication.nim.uikit.common.util.sys.TimeUtil
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageListAdapter :
    BaseQuickAdapter<RecentContact, BaseViewHolder>(R.layout.item_message_list) {
    var session_list_arr: MutableList<MessageGiftBean> = mutableListOf()//最近礼物列表

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
        GlideUtil.loadAvatorImg(
            mContext,
            UserInfoHelper.getAvatar(item.contactId),
            holder.itemView.msgIcon
        )

        if (CommonUtil.isTagSet(item, RecentContactsFragment.RECENT_TAG_STICKY)) {
            holder.itemView.menuTop.setImageResource(R.drawable.icon_top_cancel_msg)
        } else {
            holder.itemView.menuTop.setImageResource(R.drawable.icon_top_msg)
        }


        holder.itemView.msgNew.isVisible = false
        holder.itemView.text.text = CommonFunction.getRecentContent(item)

        holder.itemView.latelyTime.text = TimeUtil.getTimeShowString(item.time, true)
        if (item.unreadCount == 0) {
            holder.itemView.newCount.visibility = View.GONE
        } else {
            holder.itemView.newCount.text = "${item.unreadCount}"
            holder.itemView.newCount.visibility = View.VISIBLE
        }
        holder.itemView.msgOnLineState.isVisible =
            NimUIKitImpl.enableOnlineState()
                    && !NimUIKitImpl.getOnlineStateContentProvider()
                .getSimpleDisplay(item.contactId).isNullOrEmpty()
                    && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.contactId)
                .contains(
                    "在线"
                )


        //0 不是甜心圈 1 资产认证 2豪车认证 3身材 4职业  5高额充值

        Log.d("extensionMap","extensionMap = ${(NimUIKit.getUserInfoProvider().getUserInfo(item.contactId) as NimUserInfo).extensionMap}")
        Log.d("extensionMap","extension = ${(NimUIKit.getUserInfoProvider().getUserInfo(item.contactId) as NimUserInfo).extension}")
        val extensionMap = (NimUIKit.getUserInfoProvider().getUserInfo(item.contactId) as NimUserInfo).extensionMap
        if (!extensionMap.isNullOrEmpty() && extensionMap["assets_audit_way"] != null && extensionMap["assets_audit_way"] != 0) {
            holder.itemView.sweetLogo.isVisible = true
            if (extensionMap["assets_audit_way"] == 1 || extensionMap["assets_audit_way"] == 2|| extensionMap["assets_audit_way"] == 5) {
                holder.itemView.sweetLogo.imageAssetsFolder = "images_sweet_logo_man"
                holder.itemView.sweetLogo.setAnimation("data_sweet_logo_man.json")
            } else {
                holder.itemView.sweetLogo.imageAssetsFolder = "images_sweet_logo_woman"
                holder.itemView.sweetLogo.setAnimation("data_sweet_logo_woman.json")
            }

            if (holder.itemView.sweetLogo.tag != null) {
                holder.itemView.sweetLogo.removeOnAttachStateChangeListener(holder.itemView.sweetLogo.tag as View.OnAttachStateChangeListener)
            }

            val onAttachStateChangeListener = object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View?) {
                    holder.itemView.sweetLogo.pauseAnimation()
                }

                override fun onViewAttachedToWindow(v: View?) {
                    holder.itemView.sweetLogo.playAnimation()

                }
            }
            holder.itemView.sweetLogo.addOnAttachStateChangeListener(onAttachStateChangeListener)
            holder.itemView.sweetLogo.tag = onAttachStateChangeListener
        } else {
            holder.itemView.sweetLogo.isVisible = false
        }


    }

}