package com.sdy.jitangapplication.ui.adapter

import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import kotlinx.android.synthetic.main.item_choose_label.view.*

class ChooseLabelAdapter : BaseQuickAdapter<MyLabelBean, BaseViewHolder>(R.layout.item_choose_label) {
    override fun convert(helper: BaseViewHolder, item: MyLabelBean) {

        helper.itemView.labelName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon, SizeUtils.dp2px(8F))
        helper.itemView.labelChecked.isChecked = item.checked
    }
}