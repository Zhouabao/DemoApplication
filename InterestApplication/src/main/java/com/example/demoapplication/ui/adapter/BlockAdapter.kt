package com.example.demoapplication.ui.adapter

import android.widget.RelativeLayout
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.Photos
import kotlinx.android.synthetic.main.item_block_square.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   :九宫格形式的广场内容适配器
 *    version: 1.0
 */
class BlockAdapter : BaseQuickAdapter<Photos, BaseViewHolder>(R.layout.item_block_square) {

    override fun convert(holder: BaseViewHolder, item: Photos) {
        val params = holder.itemView.ivSquare.layoutParams as RelativeLayout.LayoutParams
        params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - SizeUtils.dp2px(10F) * 2) / 3F).toInt()
        params.height = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - SizeUtils.dp2px(10F) * 2) / 3F).toInt()
        holder.itemView.ivSquare.layoutParams = params
        GlideUtil.loadRoundImgCenterCrop(mContext, item.url ?: "", holder.itemView.ivSquare, SizeUtils.dp2px(5F))
    }

}