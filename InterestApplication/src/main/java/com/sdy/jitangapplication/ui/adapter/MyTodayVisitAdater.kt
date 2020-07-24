package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VisitorBean
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_today_visit.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 我的访客
 *    version: 1.0
 */
class MyTodayVisitAdater(var freeShow: Boolean = false) :
    BaseQuickAdapter<VisitorBean, BaseViewHolder>(R.layout.item_today_visit) {
    override fun convert(holder: BaseViewHolder, item: VisitorBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 3F)) / 2F).toInt()
        params.height = (5 / 4F * params.width).toInt()
        params.topMargin = SizeUtils.dp2px(15F)

        params.leftMargin = if ((holder.layoutPosition - headerLayoutCount) % 2 == 0) {
            SizeUtils.dp2px(15F)
        } else {
//            0
            SizeUtils.dp2px(5F)
        }
//        params.rightMargin = if ((holder.layoutPosition - headerLayoutCount) % 2 == 1) {
//            SizeUtils.dp2px(15F)
//        } else {
//            0
//        }

        holder.itemView.layoutParams = params




        if (freeShow) {
            holder.itemView.visitHideName.visibility = View.GONE
            holder.itemView.visitHideInfo.visibility = View.GONE
            GlideUtil.loadImg(mContext, item.avatar ?: "", holder.itemView.visitImg)
        } else {
            holder.itemView.visitHideName.visibility = View.VISIBLE
            holder.itemView.visitHideInfo.visibility = View.VISIBLE
            Glide.with(mContext)
                .load(item.avatar ?: "")
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                .into(holder.itemView.visitImg)
        }

        holder.itemView.visitName.text = "${item.nickname}"
        holder.itemView.visitInfo.text = "${item.age}·${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}·${item.age}·${item.distance}"

    }

}
