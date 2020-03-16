package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.PublishWayBean
import kotlinx.android.synthetic.main.item_publish_way.view.*

class PublishWayAdapter: BaseQuickAdapter<PublishWayBean, BaseViewHolder>(R.layout.item_publish_way) {
    override fun convert(helper: BaseViewHolder, item: PublishWayBean) {
        if (item.checked) {
            helper.itemView.publishWayImg.setImageResource(item.checkedImg)
            helper.itemView.publishWayIndicator.setBackgroundResource(R.drawable.shape_rectangle_black_2dp)
        } else {
            helper.itemView.publishWayImg.setImageResource(item.normalImg)
            helper.itemView.publishWayIndicator.setBackgroundResource(R.drawable.shape_rectangle_solid_white_publish_way)
        }


    }
}