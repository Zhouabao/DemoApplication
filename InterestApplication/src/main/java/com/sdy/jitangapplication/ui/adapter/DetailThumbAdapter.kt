package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.Square
import kotlinx.android.synthetic.main.item_match_detail_thumb.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2611:22
 *    desc   :广场动态封面adapter
 *    version: 1.0
 */
class DetailThumbAdapter(private var from: Int = FROM_INDEX, private var dataSize: Int = 0) :
    BaseQuickAdapter<Square, BaseViewHolder>(R.layout.item_match_detail_thumb) {


    companion object {
        val MAX_MATCH_COUNT = 4
        val FROM_INDEX = 0
        val FROM_MATCH_DETAIL = 1
    }

    override fun convert(holder: BaseViewHolder, item: Square) {
        if (from == FROM_INDEX) {
            (holder.itemView.ivThumb.layoutParams as ConstraintLayout.LayoutParams).width = SizeUtils.dp2px(50F)
            (holder.itemView.ivThumb.layoutParams as ConstraintLayout.LayoutParams).height = SizeUtils.dp2px(50F)
        } else {
            (holder.itemView.ivThumb.layoutParams as ConstraintLayout.LayoutParams).width = SizeUtils.dp2px(40F)
            (holder.itemView.ivThumb.layoutParams as ConstraintLayout.LayoutParams).height = SizeUtils.dp2px(40F)
        }
//        GlideUtil.loadImg(mContext, if (item.photo_json.isNullOrEmpty()) { item.video_json } else { item.photo_json }, holder.itemView.ivThumb)
        GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url, holder.itemView.ivThumb, SizeUtils.dp2px(5F))



        if (dataSize > MAX_MATCH_COUNT && holder.layoutPosition + 1 == MAX_MATCH_COUNT && from == FROM_INDEX) {
            holder.itemView.bgThumb.isVisible = true
            holder.itemView.lengthThumb.text = "+${dataSize - MAX_MATCH_COUNT}"
        } else {
            holder.itemView.bgThumb.isVisible = false
        }
    }


}