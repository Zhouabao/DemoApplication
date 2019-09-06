package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyCommentBean
import com.kotlin.base.common.BaseApplication.Companion.context
import kotlinx.android.synthetic.main.item_comment_child.view.*
import kotlinx.android.synthetic.main.item_comment_parent.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/1516:34
 *    desc   :
 *    version: 1.0
 */
class MyCommentAdapter : BaseQuickAdapter<MyCommentBean, BaseViewHolder>(R.layout.item_comment_parent) {

    override fun convert(holder: BaseViewHolder, item: MyCommentBean) {

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
        holder.itemView.commentDianzanNum.visibility = View.GONE
        holder.itemView.commentDianzanBtn.visibility = View.GONE
        holder.itemView.commentReplyBtn.visibility = View.GONE
    }

}