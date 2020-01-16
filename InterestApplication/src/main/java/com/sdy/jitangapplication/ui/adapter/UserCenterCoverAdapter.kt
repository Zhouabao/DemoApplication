package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.CoverSquare
import kotlinx.android.synthetic.main.item_block_square.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的动态封面适配器
 *    version: 1.0
 */
class UserCenterCoverAdapter :
    BaseQuickAdapter<CoverSquare, BaseViewHolder>(R.layout.item_block_square) {

    override fun convert(holder: BaseViewHolder, item: CoverSquare) {
        val params = holder.itemView.ivSquare.layoutParams as ConstraintLayout.LayoutParams
        params.width = SizeUtils.dp2px(98F)
        params.height =SizeUtils.dp2px(98F)
        holder.itemView.ivSquare.layoutParams = params

        holder.itemView.squareForMore.isVisible = (holder.layoutPosition == data.size && data.size > 2)
        holder.itemView.squareVideoType.isVisible = (item.type == 2 && holder.layoutPosition != data.size)//1图 2 视频
        GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", holder.itemView.ivSquare, SizeUtils.dp2px(5F))
    }

}