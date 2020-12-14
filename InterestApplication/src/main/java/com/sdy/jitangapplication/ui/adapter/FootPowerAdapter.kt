package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.FootDescr
import com.sdy.jitangapplication.model.VipDescr
import kotlinx.android.synthetic.main.item_foot_power.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 门槛会员权益
 *    version: 1.0
 */
class FootPowerAdapter :
    BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_foot_power) {
    override fun convert(helper: BaseViewHolder, item: VipDescr) {
        GlideUtil.loadImg(mContext,item.url,helper.itemView.footPowerIv)
//        GlideUtil.loadImg(mContext, item.icon, helper.itemView.footPowerIv)
        helper.itemView.footPowerContent.text = item.title

    }
}