package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_intention.view.*

class MyIntentionAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_intention) {

    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        if (helper.layoutPosition / 3 == 0) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).topMargin = SizeUtils.dp2px(20F)
        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).topMargin = SizeUtils.dp2px(0F)
        }

        helper.itemView.intentionName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.intentionIcon, SizeUtils.dp2px(0F))
        helper.itemView.intentionChecked.isVisible = item.checked


    }
}