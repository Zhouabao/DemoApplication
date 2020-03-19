package com.sdy.jitangapplication.ui.adapter

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
class AllTitlePicAdapter(private var spanCount: Int = 5) :
    BaseQuickAdapter<SquarePicBean, BaseViewHolder>(R.layout.item_title_pic) {
    override fun convert(helper: BaseViewHolder, item: SquarePicBean) {
        if (spanCount == 5) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).width =
                ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70F)) / 5f).toInt()
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).height =
                ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70F)) / 5f).toInt()
        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).width = SizeUtils.dp2px(48F)
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).height = SizeUtils.dp2px(48F)
        }

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.cover_url,
            helper.itemView.pic,
            SizeUtils.dp2px(8F)
        )
    }
}