package com.example.demoapplication.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.VideoJson
import kotlinx.android.synthetic.main.item_match_detail_img.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2418:04
 *    desc   : 加载用户多张图片的adapter
 *    version: 1.0
 */
class ListSquareImgsAdapter(
    var context: Context,
    private var datas: MutableList<VideoJson>,
    private var fullScreenWidth: Boolean = false
) :
    BaseQuickAdapter<VideoJson, BaseViewHolder>(R.layout.item_match_detail_img, datas) {

    override fun convert(holder: BaseViewHolder, item: VideoJson) {
        val position = holder.layoutPosition
        if (!fullScreenWidth) {
            if (datas.size == 1) {
                val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams
//                holder.itemView.ivUser.setType(RoundImageView.TYPE_NORMAL)
                layoutParams.width = ScreenUtils.getScreenWidth()
                layoutParams.height = SizeUtils.dp2px(252F)
                holder.itemView.ivUser.layoutParams = layoutParams
                GlideUtil.loadRoundImgCenterCrop(mContext, datas[position].url, holder.itemView.ivUser, 0)

            } else {
//                holder.itemView.ivUser.setmBorderRadius(20)
//                holder.itemView.ivUser.setType(RoundImageView.TYPE_ROUND)
                val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams
                layoutParams.width = SizeUtils.dp2px(270F)
                layoutParams.height = SizeUtils.dp2px(252F)
                layoutParams.leftMargin = SizeUtils.dp2px(10F)
                if (position == datas.size - 1) {
                    layoutParams.rightMargin = SizeUtils.dp2px(10F)
                }
                holder.itemView.ivUser.layoutParams = layoutParams
                GlideUtil.loadRoundImgCenterCrop(mContext, datas[position].url, holder.itemView.ivUser, SizeUtils.dp2px(5F))

            }

        } else {
            val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams

//            holder.itemView.ivUser.setType(RoundImageView.TYPE_NORMAL)
            layoutParams.width = ScreenUtils.getScreenWidth()
            layoutParams.height = ScreenUtils.getScreenHeight()
            holder.itemView.ivUser.layoutParams = layoutParams
            GlideUtil.loadRoundImgCenterCrop(mContext, datas[position].url, holder.itemView.ivUser, 0)

        }
    }

}