package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.FootDescr
import kotlinx.android.synthetic.main.item_foot_power.view.*
import kotlinx.android.synthetic.main.item_marquee_recommend_dating.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 门槛会员权益
 *    version: 1.0
 */
class DatingMarqueeAdapter : BaseQuickAdapter<MutableList<String>, BaseViewHolder>(R.layout.item_marquee_recommend_dating) {
    override fun convert(helper: BaseViewHolder, item: MutableList<String>) {
        val view = helper.itemView
        GlideUtil.loadCircleImg(mContext!!, item[0], view.datingIv1)
        GlideUtil.loadCircleImg(mContext!!, item[1], view.datingIv2)
        GlideUtil.loadCircleImg(mContext!!, item[2], view.datingIv3)
    }
}