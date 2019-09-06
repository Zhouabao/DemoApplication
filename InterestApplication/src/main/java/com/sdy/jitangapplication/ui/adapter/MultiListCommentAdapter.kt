package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.CommentBean
import kotlinx.android.synthetic.main.item_comment_child.view.*
import kotlinx.android.synthetic.main.item_comment_parent.view.*
import kotlinx.android.synthetic.main.layout_comment_head.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/1516:34
 *    desc   :
 *    version: 1.0
 */
class MultiListCommentAdapter(var context: Context, data: MutableList<CommentBean>) :
    BaseMultiItemQuickAdapter<CommentBean, BaseViewHolder>(data) {

    init {
        addItemType(CommentBean.TITLE, R.layout.layout_comment_head)
        addItemType(CommentBean.CONTENT, R.layout.item_comment_parent)
    }

    override fun convert(holder: BaseViewHolder, item: CommentBean) {

        when (holder.itemViewType) {
            CommentBean.TITLE -> {
                holder.itemView.allT2.text = item.content ?: ""
            }
            CommentBean.CONTENT -> {
                if (holder.layoutPosition == mData.size - 1) {
                    holder.itemView.commentDivider.visibility = View.GONE
                } else {
                    holder.itemView.commentDivider.visibility = View.VISIBLE
                }
                if (item.reply_content.isNullOrEmpty() || item.replyed_nickname.isNullOrEmpty()) {
                    holder.itemView.childView.visibility = View.GONE
                } else {
                    holder.itemView.childView.visibility = View.VISIBLE
//                    holder.addOnClickListener(R.id.childView)
                    holder.itemView.commentReplyContent.text =
                        SpanUtils.with(holder.itemView.commentReplyContent)
                            .append("${item.replyed_nickname}：")
                            .setForegroundColor(context.resources.getColor(R.color.colorBlack66))
                            .setBold()
                            .append("${item.reply_content}")
                            .create()
                }
                holder.addOnClickListener(R.id.commentDianzanBtn)
                GlideUtil.loadAvatorImg(context, item.avatar ?: "", holder.itemView.commentUser)
                holder.itemView.commentUserName.text = item.nickname ?: ""
                holder.itemView.commentTime.text = item.create_time ?: ""
                holder.itemView.commentContent.text = item.content
                holder.itemView.commentDianzanNum.text = "${item.like_count}"
                holder.itemView.commentDianzanBtn.setImageResource(
                    if (item.isliked == 1) {
                        R.drawable.icon_dianzan_red
                    } else {
                        R.drawable.icon_dianzan
                    }
                )
            }
        }
    }

}