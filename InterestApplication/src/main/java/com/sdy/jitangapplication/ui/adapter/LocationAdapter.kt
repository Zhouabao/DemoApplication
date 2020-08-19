package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.amap.api.services.core.PoiItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_location.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/2520:23
 *    desc   :
 *    version: 1.0
 */
class LocationAdapter(var checkPosition: Int = 0) :
    BaseQuickAdapter<PoiItem, BaseViewHolder>(R.layout.item_location) {

    override fun convert(holder: BaseViewHolder, item: PoiItem) {
        if (holder.layoutPosition == checkPosition) {
            holder.itemView.locationName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            holder.itemView.locationChooseImg.visibility = View.VISIBLE
        } else {
            holder.itemView.locationName.setTextColor(mContext.resources.getColor(R.color.colorBlackTitle))
            holder.itemView.locationChooseImg.visibility = View.GONE
        }
        holder.itemView.locationName.text =
//            "${item.title}${if (!item.title.isNullOrEmpty() && !item.snippet.isNullOrEmpty()) {
//                "\n" + (item.cityName ?: "") + (item.adName ?: "") + (item.businessArea ?: "") + (item.snippet ?: "")
//            } else {"" }}"
            "${(item.title ?: "")}${if (item.snippet.isNotEmpty()) {
                "\n${item.snippet}"
            } else {
                ""
            }}"
    }
}