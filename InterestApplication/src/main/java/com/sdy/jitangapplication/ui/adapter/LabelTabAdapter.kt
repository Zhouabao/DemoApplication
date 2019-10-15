package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.item_label_tab.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 标签导航栏的数据
 *    version: 1.0
 */
class LabelTabAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label_tab) {
    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        helper.itemView.labelName.text = item.title
        helper.itemView.labelIndicator.isVisible = item.checked
    }
}