package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LocationBean
import kotlinx.android.synthetic.main.item_location.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/2520:23
 *    desc   :
 *    version: 1.0
 */
class LocationAdapter(var checkPosition: Int = 0) :
    BaseQuickAdapter<LocationBean, BaseViewHolder>(R.layout.item_location) {

    override fun convert(holder: BaseViewHolder, item: LocationBean) {
        if (holder.layoutPosition == checkPosition) {
            holder.itemView.locationName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.locationChooseImg.visibility = View.VISIBLE
        } else {
            holder.itemView.locationName.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.locationChooseImg.visibility = View.GONE
        }


        SpanUtils.with(holder.itemView.locationName)
            .append(item.placeName)
            .setFontSize(12,true)
            .create()
    }
}