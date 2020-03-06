package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.setVisible
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.item_new_choose_label.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   :选中的兴趣名字
 *    version: 1.0
 */
class ChooseNewLabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_new_choose_label) {
    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        if (helper.layoutPosition == 0) {
            helper.itemView.llroot.setBackgroundResource(R.drawable.shape_rectangle_gray_btn_15dp)
            helper.itemView.labelCheckedIcon.setVisible(false)
        } else {
            helper.itemView.labelCheckedIcon.setVisible(true)
            helper.itemView.llroot.setBackgroundResource(R.drawable.selector_confirm_btn_15dp)
        }

        helper.itemView.labelName.text = item.title
    }
}