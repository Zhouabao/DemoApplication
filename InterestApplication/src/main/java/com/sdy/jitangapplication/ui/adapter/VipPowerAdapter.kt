package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VipDescr
import kotlinx.android.synthetic.main.item_vip_power.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员权益适配器
 *    version: 1.0
 */
class VipPowerAdapter : BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_vip_power) {
    override fun convert(helper: BaseViewHolder, item: VipDescr) {
        GlideUtil.loadCircleImg(mContext, item.icon_vip, helper.itemView.powerImg)
        helper.itemView.powerContent.text = item.title ?: ""
    }
}