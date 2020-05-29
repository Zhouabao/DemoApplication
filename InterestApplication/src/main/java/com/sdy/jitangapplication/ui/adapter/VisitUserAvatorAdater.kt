package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_user_center_visit_cover.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的动态封面适配器
 *    version: 1.0
 */
class VisitUserAvatorAdater(var fromMark: Boolean = false) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_user_center_visit_cover) {

    public var freeShow = false
    override fun convert(holder: BaseViewHolder, item: String) {
        val params = holder.itemView.visitCoverImg.layoutParams as RecyclerView.LayoutParams
        params.setMargins(if (holder.layoutPosition != 0) SizeUtils.dp2px(-12F) else 0, 0, 0, 0)
        if (fromMark) {
            params.width =
                (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15f * 2) - SizeUtils.dp2px(10 * 12F)) / 9 + SizeUtils.dp2px(
                    12F * 2
                )
            params.height = params.width
        } else {
            params.width = SizeUtils.dp2px(24f)
            params.height = SizeUtils.dp2px(24f)
        }

        holder.itemView.visitCoverImg.layoutParams = params
        //如果不是会员，就高斯模糊看过我的
        if (freeShow || fromMark) {
            GlideUtil.loadImg(mContext, item, holder.itemView.visitCoverImg)
        } else {
            Glide.with(mContext)
                .load(item)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                .into(holder.itemView.visitCoverImg)
        }

    }

}
