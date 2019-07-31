package com.example.demoapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.CoverSquare
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

        GlideUtil.loadImg(mContext, item.cover_url ?: "", holder.itemView.ivSquare)
    }

}