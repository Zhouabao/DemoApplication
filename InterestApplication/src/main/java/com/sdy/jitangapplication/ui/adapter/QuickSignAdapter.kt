package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_quick_sign.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/2510:52
 *    desc   :
 *    version: 1.0
 *    LabelQualityBean
 */
class QuickSignAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_quick_sign) {
    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        helper.itemView.quickSignTv.text = item.content
        if (item.cheked) {
            helper.itemView.quickSignTv.setTextColor(Color.parseColor("#FFFF6318"))
        } else {
            helper.itemView.quickSignTv.setTextColor(Color.parseColor("#FF333333"))
        }
    }
}