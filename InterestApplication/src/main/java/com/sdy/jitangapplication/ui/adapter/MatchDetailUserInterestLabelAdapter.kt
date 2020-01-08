package com.sdy.jitangapplication.ui.adapter

import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_layout_match_detail_user_interest.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/2816:26
 *    desc   :
 *    version: 1.0
 */
class MatchDetailUserInterestLabelAdapter :
    BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_layout_match_detail_user_interest) {
    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        helper.itemView.interestLabelName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.interestLabelIcon, SizeUtils.dp2px(10F))
    }
}