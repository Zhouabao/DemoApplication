package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.item_label_quality.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   : 标签特质,分三种情况，1.我的特质标记所有都为选中
 *                                 2.所有的特质，能选的为正常色儿， 不能选的为暗灰色
 *
 *    version: 1.0
 */
class LabelQualityAdapter(var myChoosed: Boolean = false) :
    BaseQuickAdapter<LabelQualityBean, BaseViewHolder>(R.layout.item_label_quality) {
    override fun convert(holder: BaseViewHolder, model: LabelQualityBean) {
        if (myChoosed) {
            holder.itemView.qualityTv.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                mContext.resources.getDrawable(R.drawable.icon_delete_label),
                null
            )
            holder.itemView.qualityTv.setBackgroundResource(R.drawable.shape_rectangle_label_choose_12dp)
            holder.itemView.qualityTv.setTextColor(mContext.resources.getColor(R.color.colorOrange))
        } else {
            holder.itemView.qualityTv.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
            if (model.checked) {
                holder.itemView.qualityTv.setBackgroundResource(R.drawable.shape_rectangle_label_choose_12dp)
                holder.itemView.qualityTv.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            } else {
                if (model.unable) {
                    holder.itemView.qualityTv.setBackgroundResource(R.drawable.shape_rectangle_label_unable_12dp)
                    holder.itemView.qualityTv.setTextColor(Color.parseColor("#FFE5E5E5"))
                } else {
                    holder.itemView.qualityTv.setBackgroundResource(R.drawable.shape_rectangle_label_normal_12dp)
                    holder.itemView.qualityTv.setTextColor(Color.parseColor("#FF787C7F"))
                }
            }
        }


        holder.itemView.qualityTv.text = model.content.trim()
    }


}