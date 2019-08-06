package com.example.demoapplication.ui.adapter

import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareMsgBean
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

        when (item.category) {
            0 -> {
                holder.itemView.squareContent.text = item.content ?: ""
                holder.itemView.squareContent.visibility = View.VISIBLE
                holder.itemView.squareImg.visibility = View.GONE
                holder.itemView.squareType.visibility = View.GONE
            }
            1 -> {
                GlideUtil.loadAvatorImg(mContext, item.cover_url ?: "", holder.itemView.msgIcon)
                holder.itemView.squareImg.visibility = View.VISIBLE
                holder.itemView.squareContent.visibility = View.GONE
                holder.itemView.squareType.visibility = View.GONE
            }
            2 -> {
                GlideUtil.loadAvatorImg(mContext, item.cover_url ?: "", holder.itemView.msgIcon)
                holder.itemView.squareImg.visibility = View.VISIBLE
                holder.itemView.squareContent.visibility = View.GONE
                holder.itemView.squareType.visibility = View.VISIBLE
                holder.itemView.squareType.setImageResource(R.drawable.icon_play_white)
            }
            3 -> {
                GlideUtil.loadAvatorImg(mContext, item.cover_url ?: "", holder.itemView.msgIcon)
                holder.itemView.squareImg.visibility = View.VISIBLE
                holder.itemView.squareContent.visibility = View.GONE
                holder.itemView.squareType.visibility = View.VISIBLE
                holder.itemView.squareType.setImageResource(R.drawable.icon_record_normal)
            }
        }


    }

}