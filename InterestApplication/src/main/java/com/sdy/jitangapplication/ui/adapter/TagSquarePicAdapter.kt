package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquarePicBean
import kotlinx.android.synthetic.main.item_title_pic.view.*

/**
 * 更多标题
 */
class TagSquarePicAdapter(private var spanCount: Int = 3) :
    BaseQuickAdapter<SquarePicBean, BaseViewHolder>(R.layout.item_title_pic) {
    override fun convert(helper: BaseViewHolder, item: SquarePicBean) {
        (helper.itemView.layoutParams as RecyclerView.LayoutParams).width =
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(50F) - SizeUtils.dp2px(5F) * (spanCount - 1)) / spanCount)
        (helper.itemView.layoutParams as RecyclerView.LayoutParams).height =
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(50F) - SizeUtils.dp2px(5F) * (spanCount - 1)) / spanCount)
        if (!item.cover_url.isNullOrEmpty()) {
            helper.itemView.pic.isVisible = true
            helper.itemView.content.isVisible = false
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item.cover_url,
                helper.itemView.pic,
                SizeUtils.dp2px(8F)
            )
        } else {
            helper.itemView.content.isVisible = true
            helper.itemView.pic.isVisible = false
            helper.itemView.content.text = item.descr
        }
    }
}