package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VipDescr
import kotlinx.android.synthetic.main.item_sweet_heart_power.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 加入甜心圈优势
 *    version: 1.0
 */
class SweetHeartPowerAdapter(val gender: Int) :
    BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_sweet_heart_power) {
    override fun convert(helper: BaseViewHolder, item: VipDescr) {
        val itemView = helper.itemView
        if (gender == 1) {
            itemView.powerTitle.setTextColor(Color.parseColor("#B2FFCD52"))
            itemView.powerContent.setTextColor(Color.parseColor("#FFFFCD52"))
            itemView.powerIndex.setTextColor(Color.parseColor("#4CFFCD52"))
            itemView.setBackgroundResource(R.drawable.rectangle_black_10dp)
        } else {
            itemView.powerTitle.setTextColor(Color.WHITE)
            itemView.powerContent.setTextColor(Color.parseColor("#B2FFFFFF"))
            itemView.powerIndex.setTextColor(Color.parseColor("#4CFFFFFF"))
            itemView.setBackgroundResource(R.drawable.shape_pink_10dp)
        }

        itemView.powerTitle.text = item.title
        itemView.powerContent.text = item.rule
        itemView.powerIndex.text = "0${helper.layoutPosition + 1}"

    }
}