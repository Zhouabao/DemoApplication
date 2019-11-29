package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.item_label_match.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/249:54
 *    desc   :匹配页面标签的adapter  标签点击更改状态并且要实时更新用户
 *    version: 1.0
 */
class MatchLabelAdapter(var context: Context, var enable: Boolean = true) :
    BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label_match) {
    override fun convert(holder: BaseViewHolder, item: NewLabel) {
        holder.itemView.labelTv.text = item.title
        if (item.checked) {
            holder.itemView.labelTv.setTextColor(Color.parseColor("#FF191919"))
            holder.itemView.labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
            holder.itemView.labelTv.paint.isFakeBoldText = true
            holder.itemView.labelCheckIv.isVisible = true
        } else {
            holder.itemView.labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)
            holder.itemView.labelTv.setTextColor(Color.parseColor("#FFA1A1A1"))
            holder.itemView.labelTv.paint.isFakeBoldText = false
            holder.itemView.labelCheckIv.isVisible = false
        }
    }

}