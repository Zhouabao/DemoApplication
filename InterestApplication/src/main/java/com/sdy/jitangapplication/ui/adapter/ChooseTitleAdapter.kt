package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_choose_title.view.*

/**
 * 选择话题
 */
class ChooseTitleAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_choose_title) {
    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        if (item.isfuse) {
            helper.itemView.titleName.setBackgroundResource(R.drawable.shape_rectangle_blue_16dp)
            helper.itemView.titleName.setTextColor(Color.parseColor("#FF6796FA"))
        } else {
            helper.itemView.titleName.setBackgroundResource(R.drawable.shape_rectangle_gray_16dp)
            helper.itemView.titleName.setTextColor(Color.parseColor("#FF787C7F"))
        }
        helper.itemView.titleName.text = item.content
    }
}