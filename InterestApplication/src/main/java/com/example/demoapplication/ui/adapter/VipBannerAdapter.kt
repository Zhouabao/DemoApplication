package com.example.demoapplication.ui.adapter

import android.widget.LinearLayout
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.VipDescr
import kotlinx.android.synthetic.main.item_vip_banner.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipBannerAdapter : BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_vip_banner) {
    override fun convert(holder: BaseViewHolder, data: VipDescr) {
        holder.itemView.banner_name.text = "${data.title}"
        holder.itemView.banner_content.text = "${data.rule}"

        val params = holder.itemView.banner_img.layoutParams
        params.height = SizeUtils.dp2px(130F)
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT
        holder.itemView.banner_img.layoutParams = params
        GlideUtil.loadRoundImgCenterinside(mContext, data.url ?: "", holder.itemView.banner_img, 0.1F, 0)
    }
}