package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VipDescr
import kotlinx.android.synthetic.main.item_vip_banner.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipBannerAdapter() : BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_vip_banner) {
    override fun convert(holder: BaseViewHolder, data: VipDescr) {
        holder.itemView.banner_name.text = "${data.title}"
        holder.itemView.banner_content.text = "${data.rule}"
        GlideUtil.loadImgCenterCrop(mContext, data.icon_vip ?: "", holder.itemView.banner_img)
    }
}