package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.business.recent.RecentContactsFragment
import com.netease.nim.uikit.business.session.module.list.MsgAdapter
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.CommonUtil
import com.netease.nim.uikit.common.util.sys.TimeUtil
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.Likelist
import com.sdy.jitangapplication.model.MessageGiftBean
import com.sdy.jitangapplication.nim.attachment.*
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_message_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *    version: 1.0
 */
class MessageListAdapter :
    BaseQuickAdapter<RecentContact, BaseViewHolder>(R.layout.item_message_list) {
    var greetList: MutableList<Likelist> = mutableListOf()//招呼列表
    var intentionMatchList: MutableList<String> = mutableListOf()//意向匹配列表
    var session_list_arr: MutableList<MessageGiftBean> = mutableListOf()//最近礼物列表
    var isapprove: Int = 0  //0 不验证  1去认证 2去开通会员  3去认证+去会员  4去会员+去认证
    var approveTime: Long = 1579104000L  //0 不验证  1去认证 2去开通会员  3去认证+去会员  4去会员+去认证

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

        if (item.fromAccount != UserManager.getAccid() && item.attachment !is ChatHiAttachment && UserManager.approveBean != null && UserManager.approveBean!!.isapprove != 0 && item.time / 1000 >= UserManager.approveBean!!.approve_time) {
            holder.itemView.text.text = "有消息未查看"
            holder.itemView.msgNew.isVisible = true
        } else {
            holder.itemView.msgNew.isVisible = false
            when (item.attachment) {
                is ChatHiAttachment -> holder.itemView.text.text =
                    when ((item.attachment as ChatHiAttachment).showType) {
                        ChatHiAttachment.CHATHI_HI -> "『招呼消息』"
                        ChatHiAttachment.CHATHI_MATCH -> "『匹配消息』"
                        ChatHiAttachment.CHATHI_RFIEND -> "『好友消息』"
                        ChatHiAttachment.CHATHI_OUTTIME -> "『消息过期』"
                        else -> ""
                    }
                is ShareSquareAttachment -> holder.itemView.text.text = "『动态分享内容』"
                is SendGiftAttachment -> {
                    var hasList = false
                    for (data in session_list_arr) {
                        if ((item.attachment as SendGiftAttachment).id == data.id) {
                            when (data.state) {
                                SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL -> {
                                    if (item.fromAccount == UserManager.getAccid()) {
                                        holder.itemView.text.text = "[礼物]糖果礼物待领取"
                                    } else {

                                        holder.itemView.text.text =
                                            SpanUtils.with(holder.itemView.text)
                                                .append("[礼物]")
                                                .setForegroundColor(Color.parseColor("#FFFD4417"))
                                                .append("糖果礼物待领取")
                                                .setForegroundColor(Color.parseColor("#FFCCCDCF"))
                                                .create()
                                    }
                                }
                                SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN -> {
                                    holder.itemView.text.text = "[礼物]糖果礼物已领取"
                                }
                                SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED -> {
                                    holder.itemView.text.text = "[礼物]糖果礼物已退回"
                                }
                            }
                            hasList = true
                            break
                        }
                    }
                    if (!hasList) {
                        when ((item.attachment as SendGiftAttachment).giftStatus) {
                            SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL -> {
                                if (item.fromAccount == UserManager.getAccid()) {
                                    holder.itemView.text.text = "[礼物]糖果礼物待领取"
                                } else {

                                    holder.itemView.text.text =
                                        SpanUtils.with(holder.itemView.text)
                                            .append("[礼物]")
                                            .setForegroundColor(Color.parseColor("#FFFD4417"))
                                            .append("糖果礼物待领取")
                                            .setForegroundColor(Color.parseColor("#FFCCCDCF"))
                                            .create()
                                }
                            }
                            SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN -> {
                                holder.itemView.text.text = "[礼物]糖果礼物已领取"
                            }
                            SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED -> {
                                holder.itemView.text.text = "[礼物]糖果礼物已退回"
                            }
                        }
                    }
                }
                is WishHelpAttachment -> {
                    var hasList = false
                    for (data in session_list_arr) {
                        if ((item.attachment as WishHelpAttachment).orderId == data.id) {
                            when (data.state) {
                                WishHelpAttachment.WISH_HELP_STATUS_NORMAL -> {
                                    if (item.fromAccount == UserManager.getAccid()) {
                                        holder.itemView.text.text = "[助力]助力额度${(item.attachment as WishHelpAttachment).amount}糖果"
                                    } else {
                                        holder.itemView.text.text = SpanUtils.with(holder.itemView.text)
                                            .append("[助力]")
                                            .setForegroundColor(Color.parseColor("#FFFD4417"))
                                            .append("助力额度${(item.attachment as WishHelpAttachment).amount}糖果")
                                            .setForegroundColor(Color.parseColor("#FFCCCDCF"))
                                            .create()
                                    }
                                }
                                WishHelpAttachment.WISH_HELP_STATUS_HAS_OPEN -> {
                                    holder.itemView.text.text =
                                        "[助力]助力额度${(item.attachment as WishHelpAttachment).amount}糖果"
                                }
                                WishHelpAttachment.WISH_HELP_STATUS_HAS_RETURNED -> {
                                    holder.itemView.text.text =
                                        "[助力]助力额度${(item.attachment as WishHelpAttachment).amount}糖果"
                                }
                            }
                            hasList = true
                            break
                        }
                    }
                    if (!hasList) {
                        when ((item.attachment as WishHelpAttachment).wishHelpStatus) {
                            WishHelpAttachment.WISH_HELP_STATUS_NORMAL -> {
                                if (item.fromAccount == UserManager.getAccid()) {
                                    holder.itemView.text.text = "[助力]助力额度${(item.attachment as WishHelpAttachment).amount}糖果"
                                } else {
                                    holder.itemView.text.text = SpanUtils.with(holder.itemView.text)
                                        .append("[助力]")
                                        .setForegroundColor(Color.parseColor("#FFFD4417"))
                                        .append("助力额度${(item.attachment as WishHelpAttachment).amount}糖果")
                                        .setForegroundColor(Color.parseColor("#FFCCCDCF"))
                                        .create()
                                }
                            }
                            WishHelpAttachment.WISH_HELP_STATUS_HAS_OPEN -> {
                                holder.itemView.text.text =
                                    "[助力]助力额度${(item.attachment as WishHelpAttachment).amount}糖果"
                            }
                            WishHelpAttachment.WISH_HELP_STATUS_HAS_RETURNED -> {
                                holder.itemView.text.text =
                                    "[助力]助力额度${(item.attachment as WishHelpAttachment).amount}糖果"
                            }
                        }
                    }
                }


                is SendCustomTipAttachment -> holder.itemView.text.text =
                    (item.attachment as SendCustomTipAttachment).content
                else -> holder.itemView.text.text = item.content
            }
        }

        holder.itemView.latelyTime.text = TimeUtil.getTimeShowString(item.time, true)
        if (item.unreadCount == 0) {
            holder.itemView.newCount.visibility = View.GONE
        } else {
            holder.itemView.newCount.text = "${item.unreadCount}"
            holder.itemView.newCount.visibility = View.VISIBLE
        }
        Log.d(MsgAdapter::class.java.simpleName, greetList.toString())
        holder.itemView.msgTag.isVisible =
            greetList.toString().contains(item.contactId) || intentionMatchList.contains(item.contactId)
        if (greetList.toString().contains(item.contactId)) {
            holder.itemView.msgTag.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.msgTag.setBackgroundResource(R.drawable.shape_rectangle_lightorange_5dp)
        } else if (intentionMatchList.contains(item.contactId)) {
            holder.itemView.msgTag.setTextColor(mContext.resources.getColor(R.color.colorBlue))
            holder.itemView.msgTag.setBackgroundResource(R.drawable.shape_rectangle_blue_5dp)
        }


        holder.itemView.msgOnLineState.isVisible =
            NimUIKitImpl.enableOnlineState()
                    && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.contactId).isNullOrEmpty()
                    && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.contactId).contains(
                "在线"
            )


    }

}