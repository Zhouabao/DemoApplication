package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VisitorBean
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_visit.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 我的访客
 *    version: 1.0
 */
class MyVisitAdater(var freeShow: Boolean = false) :
    BaseQuickAdapter<VisitorBean, BaseViewHolder>(R.layout.item_visit) {


    override fun convert(holder: BaseViewHolder, item: VisitorBean) {
        if (freeShow) {
            holder.itemView.visitHideName.visibility = View.GONE
            holder.itemView.visitHideInfo.visibility = View.GONE
            GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.visitImg)
        } else {
            holder.itemView.visitHideName.visibility = View.VISIBLE
            holder.itemView.visitHideInfo.visibility = View.VISIBLE
            Glide.with(mContext)
                .load(item.avatar ?: "")
                .apply(RequestOptions.bitmapTransform(BlurTransformation(1,10)))
                .into(holder.itemView.visitImg)
        }

        holder.itemView.visitName.text = "${item.nickname}"
        holder.itemView.visitInfo.text = "${item.age}\t/\t${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}\t/\t${item.constellation}\t/\t${item.distance}"
        holder.itemView.visitCount.text = "${item.visitcount}"
        holder.itemView.visitVip.isVisible = (item.isvip ?: 0) == 1

    }

}
