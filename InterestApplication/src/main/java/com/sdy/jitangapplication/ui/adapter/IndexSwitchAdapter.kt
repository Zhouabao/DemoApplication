package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareTitleBean
import kotlinx.android.synthetic.main.item_index_switch.view.*

class IndexSwitchAdapter : BaseQuickAdapter<SquareTitleBean, BaseViewHolder>(R.layout.item_index_switch) {
    override fun convert(helper: BaseViewHolder, item: SquareTitleBean) {
        helper.itemView.title.text = item.title
        if (item.checked) {
            helper.itemView.title.paint.isFakeBoldText = true
            helper.itemView.image.setImageResource(item.imageChecked)
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_white_6dp)
            helper.itemView.title.setTextColor(mContext.resources.getColor(R.color.colorOrange))
        } else {
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_f6f6f6_6dp)
            helper.itemView.image.setImageResource(item.image)
            helper.itemView.title.paint.isFakeBoldText = false
            helper.itemView.title.setTextColor(mContext.resources.getColor(R.color.colorBlack8D))

        }
    }

}