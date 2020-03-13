package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareTitleBean
import kotlinx.android.synthetic.main.item_square_switch.view.*

class SquareSwitchAdapter : BaseQuickAdapter<SquareTitleBean, BaseViewHolder>(R.layout.item_square_switch) {
    override fun convert(helper: BaseViewHolder, item: SquareTitleBean) {
        helper.itemView.title.text = item.title
        helper.itemView.titleCheck.isVisible = item.checked
        if (item.checked) {
            helper.itemView.title.textSize = 24F
            helper.itemView.title.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            helper.itemView.title.paint.isFakeBoldText = true
        } else {
            helper.itemView.title.textSize = 16F
            helper.itemView.title.setTextColor(Color.parseColor("#ff191919"))
            helper.itemView.title.paint.isFakeBoldText = false
        }
    }

}