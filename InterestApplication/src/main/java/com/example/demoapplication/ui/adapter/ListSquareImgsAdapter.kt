package com.example.demoapplication.ui.adapter

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.baselibrary.widgets.RoundImageView
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
        val layoutParams = holder.itemView.ivUser.layoutParams as RecyclerView.LayoutParams
        if (!fullScreenWidth) {
            if (datas.size == 1) {
                holder.itemView.ivUser.setType(RoundImageView.TYPE_NORMAL)
                layoutParams.width = ScreenUtils.getScreenWidth()
                layoutParams.height = SizeUtils.applyDimension(252F, TypedValue.COMPLEX_UNIT_DIP).toInt()
                holder.itemView.ivUser.layoutParams = layoutParams

            } else {
                holder.itemView.ivUser.setmBorderRadius(20)
                holder.itemView.ivUser.setType(RoundImageView.TYPE_ROUND)
                layoutParams.width = SizeUtils.applyDimension(270F, TypedValue.COMPLEX_UNIT_DIP).toInt()
                layoutParams.height = SizeUtils.applyDimension(252F, TypedValue.COMPLEX_UNIT_DIP).toInt()
                layoutParams.leftMargin = SizeUtils.applyDimension(10F, TypedValue.COMPLEX_UNIT_DIP).toInt()
                if (position == datas.size - 1) {
                    layoutParams.rightMargin = SizeUtils.applyDimension(10F, TypedValue.COMPLEX_UNIT_DIP).toInt()
                }
                holder.itemView.ivUser.layoutParams = layoutParams
            }

        } else {
            holder.itemView.ivUser.setType(RoundImageView.TYPE_NORMAL)
            layoutParams.width = ScreenUtils.getScreenWidth()
            layoutParams.height = ScreenUtils.getScreenHeight()
            holder.itemView.ivUser.layoutParams = layoutParams
        }
        GlideUtil.loadImg(context, datas[position].url, holder.itemView.ivUser)
    }

}