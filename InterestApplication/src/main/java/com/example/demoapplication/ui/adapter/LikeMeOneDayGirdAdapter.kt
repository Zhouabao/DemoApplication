package com.example.demoapplication.ui.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.LikeMeBean
import com.example.demoapplication.utils.UserManager
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_like_me_one_day_all.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   :
 *    version: 1.0
 */
class LikeMeOneDayGirdAdapter : BaseQuickAdapter<LikeMeBean, BaseViewHolder>(R.layout.item_like_me_one_day_all) {
    override fun convert(holder: BaseViewHolder, item: LikeMeBean) {
        holder.addOnClickListener(R.id.likeMeCount)
        val itemView = holder.itemView
        val params = itemView.likeMeAvator.layoutParams as ConstraintLayout.LayoutParams
        params.width = SizeUtils.dp2px(180F)
        params.height = (16 / 9F * params.width).toInt()
        itemView.likeMeAvator.layoutParams = params
        GlideUtil.loadImg(mContext, item.avatar, itemView.likeMeAvator)
        if (UserManager.isUserVip()) {
            itemView.likeMeType.visibility = View.VISIBLE
        } else {
            itemView.likeMeType.visibility = View.INVISIBLE
            Glide.with(mContext)
                .load(itemView.likeMeAvator)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                .into(holder.itemView.likeMeAvator)
        }


    }
}