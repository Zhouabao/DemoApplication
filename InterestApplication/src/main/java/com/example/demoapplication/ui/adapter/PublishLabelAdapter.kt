package com.example.demoapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.LabelBean
import kotlinx.android.synthetic.main.item_label_publish_index.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/249:54
 *    desc   : 发布页面标签的adapter  标签点击进行删除
 *    version: 1.0
 */
class PublishLabelAdapter : BaseQuickAdapter<LabelBean, BaseViewHolder>(R.layout.item_label_publish_index) {
    override fun convert(holder: BaseViewHolder, item: LabelBean) {
        if (holder.layoutPosition == 0) {
            holder.itemView.publishLabelIcon.visibility = View.GONE
            holder.itemView.publishLabelTv.text = "添加标签"
            holder.itemView.publishLabelTv.setTextColor(mContext.resources.getColor(R.color.colorGrayText))
            holder.itemView.setBackgroundResource(R.drawable.shape_rectangle_gray_white_15dp)
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.leftMargin = SizeUtils.dp2px(15F)
            holder.itemView.layoutParams = params
        } else {
            holder.itemView.publishLabelIcon.visibility = View.VISIBLE
            holder.itemView.publishLabelTv.text = item.title
            holder.itemView.publishLabelTv.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.setBackgroundResource(R.drawable.shape_rectangle_orange_15dp)
        }

    }


}