package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VipDescr
import com.sdy.jitangapplication.model.VipPowerBean
import kotlinx.android.synthetic.main.item_vip_power.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员权益适配器
 *    version: 1.0
 */
class VipPowerAdapter(val type: Int) :
    BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_vip_power) {
    override fun convert(helper: BaseViewHolder, item: VipDescr) {
        GlideUtil.loadCircleImg(mContext, item.icon_vip, helper.itemView.powerImg)
        helper.itemView.powerTitle.text = item.title ?: ""
        helper.itemView.powerContent.text = item.rule ?: ""
        if (type == VipPowerBean.TYPE_CONTACT_CARD) {
            helper.itemView.powerContent.setTextColor(Color.parseColor("#ffcd7e14"))
        } else {
            helper.itemView.powerContent.setTextColor(Color.parseColor("#FF5E6473"))
        }
    }
}