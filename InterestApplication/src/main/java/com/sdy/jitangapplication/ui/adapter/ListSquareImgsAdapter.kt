package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VideoJson
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
//            if (datas.size == 1) {
//                val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams
//                layoutParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(30F)
//                layoutParams.height = layoutParams.width
//                layoutParams.leftMargin = SizeUtils.dp2px(15F)
//                layoutParams.rightMargin = SizeUtils.dp2px(15F)
//
//                holder.itemView.ivUser.layoutParams = layoutParams
//                GlideUtil.loadRoundImgCenterCrop(
//                    mContext,
//                    datas[position].url,
//                    holder.itemView.ivUser,
//                    SizeUtils.dp2px(5F)
//                )
//
//            } else {
                val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams
                layoutParams.width = SizeUtils.dp2px(250F)
                layoutParams.height = SizeUtils.dp2px(250F)
                layoutParams.leftMargin = SizeUtils.dp2px(15F)
                if (position == datas.size - 1) {
                    layoutParams.rightMargin = SizeUtils.dp2px(15F)
                }
                holder.itemView.ivUser.layoutParams = layoutParams
                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    datas[position].url,
                    holder.itemView.ivUser,
                    SizeUtils.dp2px(5F)
                )

//            }

        } else {
            val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams
            layoutParams.width = ScreenUtils.getScreenWidth()
            layoutParams.height = ScreenUtils.getScreenHeight()
            holder.itemView.ivUser.layoutParams = layoutParams
            GlideUtil.loadRoundImgCenterCrop(mContext, datas[position].url, holder.itemView.ivUser, 0)

        }
    }

}