package com.sdy.jitangapplication.ui.adapter

import android.view.View
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
        if (item.type == 2) { //1图 2 视频
            holder.itemView.squareVideoType.visibility = View.VISIBLE
        } else {
            holder.itemView.squareVideoType.visibility = View.GONE
        }
        GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", holder.itemView.ivSquare,  SizeUtils.dp2px(5F))
    }

}