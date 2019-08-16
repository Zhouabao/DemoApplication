package com.example.demoapplication.ui.adapter

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

        GlideUtil.loadRoundImgCenterCrop(mContext, item.url ?: "", holder.itemView.ivSquare,  SizeUtils.dp2px(5F))
    }

}