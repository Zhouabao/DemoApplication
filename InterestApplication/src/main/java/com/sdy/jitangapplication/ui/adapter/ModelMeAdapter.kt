package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_layout_model_me.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/117:36
 *    desc   :
 *    version: 1.0
 */
class ModelMeAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_layout_model_me) {

    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {

        helper.itemView.modelTitle.text = item.title
        helper.itemView.modelContent.text = item.content


    }
}