package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_intention.view.*

class MyIntentionAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_intention) {

    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        (helper.itemView.layoutParams as RecyclerView.LayoutParams).height = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(45F)) / 2
        helper.itemView.intentionName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.intentionIcon, SizeUtils.dp2px(15F))
        if (item.isfuse) {
            helper.itemView.intentionName.setBackgroundResource(R.drawable.shape_rectangle_orange_transparent_bottom_left_right_15dp)
        } else {
            helper.itemView.intentionName.setBackgroundResource(R.drawable.shape_rectangle_gray_transparent_bottom_left_right_15dp)
        }


    }
}