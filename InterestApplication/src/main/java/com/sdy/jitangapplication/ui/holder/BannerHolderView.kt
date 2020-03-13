package com.sdy.jitangapplication.ui.holder

import android.view.View
import com.blankj.utilcode.util.SizeUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareBannerBean
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.item_head_recommend_banner.view.*

class BannerHolderView : ViewHolder<SquareBannerBean> {
    override fun getLayoutId(): Int {
        return R.layout.item_head_recommend_banner
    }

    override fun onBind(itemView: View, data: SquareBannerBean, position: Int, size: Int) {
        GlideUtil.loadRoundImgCenterCrop(itemView.bannerImg.context, data.cover_url, itemView.bannerImg, SizeUtils.dp2px(10F))
    }
}