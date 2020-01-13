package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.Square
import kotlinx.android.synthetic.main.item_match_detail_thumb.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   :
 *    version: 1.0
 */
class ChatTaregetSquareAdapter(var dataSize: Int = 0) :
    BaseQuickAdapter<Square, BaseViewHolder>(R.layout.item_chat_hi_label) {
    companion object {
        public const val MAX_SHOW_COUNT = 5
    }

    override fun convert(holder: BaseViewHolder, item: Square) {
        (holder.itemView.ivThumb.layoutParams as ConstraintLayout.LayoutParams).width =
            (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(110F)) / 5
        (holder.itemView.ivThumb.layoutParams as ConstraintLayout.LayoutParams).height =
            (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(110F)) / 5

        GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url, holder.itemView.ivThumb, SizeUtils.dp2px(6F))

        if (holder.layoutPosition + 1 == MAX_SHOW_COUNT && dataSize > MAX_SHOW_COUNT) {
            holder.itemView.bgThumb.isVisible = true
            holder.itemView.lengthThumb.text = "+${dataSize - MAX_SHOW_COUNT}"
        } else {
            holder.itemView.bgThumb.isVisible = false
        }
    }
}