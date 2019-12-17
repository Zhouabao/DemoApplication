package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_title_class.view.*

/**
 * 更多标题头部导航适配器
 */
class AllTitleNavAdapter : BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_title_class) {

    override fun convert(helper: BaseViewHolder, item: LabelQualityBean) {
        if (item.checked) {
            helper.itemView.titleClassTv.setBackgroundResource(R.drawable.shape_rectangle_gray_14dp)
            helper.itemView.titleClassTv.setTextColor(Color.parseColor("#191919"))
            helper.itemView.titleClassTv.paint.isFakeBoldText = true
        } else {
            helper.itemView.titleClassTv.setBackgroundResource(R.drawable.shape_rectangle_white_14dp)
            helper.itemView.titleClassTv.setTextColor(Color.parseColor("#888D92"))
            helper.itemView.titleClassTv.paint.isFakeBoldText = false
        }
        helper.itemView.titleClassTv.text = item.title
    }
}