package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_choose_title.view.*

class ChooseTitleAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_choose_title) {
    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        if (item.checked) {
            helper.itemView.titleName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
        } else {
            helper.itemView.titleName.setTextColor(Color.parseColor("#191919"))
        }
        helper.itemView.titleChecked.isVisible = item.checked
        helper.itemView.titleName.text = item.content
    }
}