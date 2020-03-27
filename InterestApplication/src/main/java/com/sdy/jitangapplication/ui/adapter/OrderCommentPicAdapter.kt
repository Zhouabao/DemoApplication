package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_order_comment.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2611:39
 *    desc   :
 *    version: 1.0
 */
class OrderCommentPicAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_order_comment) {
    override fun convert(helper: BaseViewHolder, item: String) {
        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.rightMargin = if (helper.layoutPosition == mData.size - 1) {
            SizeUtils.dp2px(0F)
        } else {
            SizeUtils.dp2px(10f)
        }


        helper.itemView.addPicComment.isVisible = helper.layoutPosition == 0
        helper.itemView.ivPic.isVisible = helper.layoutPosition != 0
        helper.itemView.cancelPic.isVisible = helper.layoutPosition != 0

        helper.addOnClickListener(R.id.addPicComment)
        helper.addOnClickListener(R.id.cancelPic)

        if (helper.layoutPosition != 0)
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item,
                helper.itemView.ivPic,
                SizeUtils.dp2px(8F)
            )
    }
}