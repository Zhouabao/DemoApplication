package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.item_label.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   : 此处存在二级标签和三级标签的选择和反选
 *    version: 1.0
 */
class LabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label) {
    override fun convert(holder: BaseViewHolder, model: NewLabel) {
        if (model.checked) {
            holder.itemView.llroot.setBackgroundResource(R.drawable.cb_label_checked)
            holder.itemView.checkedIcon.visibility = View.VISIBLE
            holder.itemView.labelTv.setTextColor(mContext.resources.getColor(R.color.colorOrange))
        } else {
            holder.itemView.llroot.setBackgroundResource(R.drawable.cb_label_unchecked)
            holder.itemView.checkedIcon.visibility = View.GONE
            holder.itemView.labelTv.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
        }

//        holder.itemView.onClick {
//            mItemClickListener?.onItemClick(model, position)
//        }
//
//        if (model.checked) {
//            holder.itemView.llroot.setBackgroundResource(R.drawable.cb_label_checked)
//            holder.itemView.checkedIcon.visibility = View.VISIBLE
//            holder.itemView.labelTv.isChecked = true
//        } else {
//            holder.itemView.llroot.setBackgroundResource(R.drawable.cb_label_unchecked)
//            holder.itemView.checkedIcon.visibility = View.GONE
//            holder.itemView.labelTv.isChecked = false
//        }

//        holder.itemView.labelTv.isChecked = model.checked
        holder.itemView.labelTv.text = model.title
    }


}