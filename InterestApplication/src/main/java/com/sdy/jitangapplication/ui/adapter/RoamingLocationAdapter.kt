package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.City
import com.sdy.jitangapplication.model.CityBean
import com.sdy.jitangapplication.model.ContactBean
import com.sdy.jitangapplication.model.ProviceBean
import kotlinx.android.synthetic.main.item_roaming_location.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/614:30
 *    desc   :
 *    version: 1.0
 */
class RoamingLocationAdapter : BaseQuickAdapter<CityBean, BaseViewHolder>(R.layout.item_roaming_location) {
    override fun convert(helper: BaseViewHolder, item: CityBean) {
        val position = helper.layoutPosition
        //因为添加了头部 所以位置要移动
        if ((position == 0 || data[position - 1].index != item.index)) {
            helper.itemView.indexTv.visibility = View.VISIBLE
            helper.itemView.indexDivider.visibility = View.VISIBLE
        } else {
            helper.itemView.indexTv.visibility = View.GONE
            helper.itemView.indexDivider.visibility = View.GONE
        }
        helper.itemView.cityName.text = item.name
        helper.itemView.indexTv.text = item.index

    }

}