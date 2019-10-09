package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabelBean
import kotlinx.android.synthetic.main.item_label_tab.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   :
 *    version: 1.0
 */
class LabelTabAdapter : BaseQuickAdapter<NewLabelBean, BaseViewHolder>(R.layout.item_label_tab) {
    override fun convert(helper: BaseViewHolder, item: NewLabelBean) {
        helper.itemView.labelName.text = item.parent
        helper.itemView.labelIndicator.isVisible = item.checked
    }
}