package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_layout_model_label_introduce.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/117:36
 *    desc   :
 *    version: 1.0
 */
class ModelLabelIntroduceAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_layout_model_label_introduce) {

    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {

        helper.itemView.nameTv.text = item.title
        helper.itemView.introduceTv.text = item.content
        GlideUtil.loadCircleImg(mContext,item.icon,helper.itemView.avatorCiv)
    }
}