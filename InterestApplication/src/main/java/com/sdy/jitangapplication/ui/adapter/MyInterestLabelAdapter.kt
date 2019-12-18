package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_label_my_interest.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 我感兴趣的标签
 *    version: 1.0
 */
class MyInterestLabelAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_label_my_interest) {

    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        helper.itemView.labelDelete.isVisible = item.isfuse
        helper.itemView.labelName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelImg, SizeUtils.dp2px(15F))
        helper.addOnClickListener(R.id.labelDelete)
    }
}