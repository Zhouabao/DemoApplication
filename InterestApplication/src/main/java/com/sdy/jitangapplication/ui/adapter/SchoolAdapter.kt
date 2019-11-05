package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SchoolBean
import kotlinx.android.synthetic.main.item_layout_school.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/414:54
 *    desc   :
 *    version: 1.0
 */
class SchoolAdapter : BaseQuickAdapter<SchoolBean, BaseViewHolder>(R.layout.item_layout_school) {

    override fun convert(helper: BaseViewHolder, item: SchoolBean) {
        helper.itemView.schoolName.text = item.school_title
    }
}