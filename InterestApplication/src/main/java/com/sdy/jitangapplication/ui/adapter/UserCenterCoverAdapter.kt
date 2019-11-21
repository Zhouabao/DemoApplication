package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
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
        params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - SizeUtils.dp2px(10F) * 2) / 3F).toInt()
        params.height = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - SizeUtils.dp2px(10F) * 2) / 3F).toInt()
        holder.itemView.ivSquare.layoutParams = params

        holder.itemView.squareForMore.isVisible = holder.layoutPosition == data.size
        holder.itemView.squareVideoType.isVisible = (item.type == 2 && holder.layoutPosition != data.size)//1图 2 视频
        GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", holder.itemView.ivSquare, SizeUtils.dp2px(5F))
    }

}