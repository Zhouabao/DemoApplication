package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareMsgBean
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_message_square_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   :
 *     //类型 1，广场点赞 2，评论我的 3。我的评论点赞的 4 @我的
 *     //发布消息的类型 0,纯文本的 1，照片 2，视频 3，声音
 *    version: 1.0
 */
class MessageSquareAdapter : BaseQuickAdapter<SquareMsgBean, BaseViewHolder>(R.layout.item_message_square_list) {
    override fun convert(holder: BaseViewHolder, item: SquareMsgBean) {
        holder.addOnClickListener(R.id.msgIcon)
        //未读
        if (item.is_read == false && item.pos == 0) {
            holder.itemView.headTitle.text = "未读消息"
            holder.itemView.headTitle.isVisible = true
        } else if (item.is_read == true && item.pos == 0) {
            holder.itemView.headTitle.text = "历史消息"
            holder.itemView.headTitle.isVisible = true
        } else {
            holder.itemView.headTitle.isVisible = false
        }

        GlideUtil.loadAvatorImg(mContext, item.avatar, holder.itemView.msgIcon)
        holder.itemView.msgTitle.text = item.nickname ?: ""
        holder.itemView.text.text = when (item.type) {
            1 -> {
                "赞了你的动态"
            }
            2 -> {
                SpanUtils.with(holder.itemView.text)
                    .append("评论\t\t")
                    .setForegroundColor(mContext.resources.getColor(R.color.colorBlack99))
                    .append(item.content ?: "")
                    .setForegroundColor(mContext.resources.getColor(R.color.colorBlackTitle))
                    .create()
            }
            3 -> {
                "赞了你的评论"
            }
            4 -> {
                "发布了动态@了你"
            }
            else -> {
                ""
            }
        }

        when (item.category) {   //	0文本 1图片 2视频 3 语音
            0 -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.msgIcon)
                holder.itemView.squareContent.text = item.cover_url ?: ""
                holder.itemView.squareContent.visibility = View.VISIBLE
                holder.itemView.squareImg.visibility = View.GONE
                holder.itemView.squareType.visibility = View.GONE
            }
            1 -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.msgIcon)
                GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", holder.itemView.squareImg,SizeUtils.dp2px(5F))
                holder.itemView.squareImg.visibility = View.VISIBLE
                holder.itemView.squareContent.visibility = View.GONE
                holder.itemView.squareType.visibility = View.GONE
            }
            2 -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.msgIcon)
                GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", holder.itemView.squareImg,SizeUtils.dp2px(5F))
                holder.itemView.squareImg.visibility = View.VISIBLE
                holder.itemView.squareContent.visibility = View.GONE
                holder.itemView.squareType.visibility = View.VISIBLE
                holder.itemView.squareType.setImageResource(R.drawable.icon_type_video)
            }
            3 -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.msgIcon)
                GlideUtil.loadRoundImgCenterCrop(mContext, UserManager.getAvator(), holder.itemView.squareImg,SizeUtils.dp2px(5F))
                holder.itemView.squareImg.visibility = View.VISIBLE
                holder.itemView.squareContent.visibility = View.GONE
                holder.itemView.squareType.visibility = View.VISIBLE
                holder.itemView.squareType.setImageResource(R.drawable.icon_type_audio)
            }
        }

    }

}