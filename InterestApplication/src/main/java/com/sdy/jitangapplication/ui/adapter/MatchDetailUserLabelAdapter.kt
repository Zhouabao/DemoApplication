package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import kotlinx.android.synthetic.main.item_layout_match_detail_user.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/2816:26
 *    desc   :
 *    version: 1.0
 */
class MatchDetailUserLabelAdapter :
    BaseQuickAdapter<MyLabelBean, BaseViewHolder>(R.layout.item_layout_match_detail_user) {
    override fun convert(helper: BaseViewHolder, item: MyLabelBean) {
        helper.itemView.labelName.text = item.title
        GlideUtil.loadImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon)
        val labelQualityMyAdapter = LabelQualityAdapter()
        val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        helper.itemView.labelQualityRv.layoutManager = manager
        helper.itemView.labelQualityRv.adapter = labelQualityMyAdapter
        labelQualityMyAdapter.setNewData(item.label_quality)
    }
}