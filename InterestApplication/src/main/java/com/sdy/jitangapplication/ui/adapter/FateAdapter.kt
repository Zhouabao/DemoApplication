package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NearPersonBean
import kotlinx.android.synthetic.main.item_fate_person.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2715:53
 *    desc   : 今日缘分
 *    version: 1.0
 */
class FateAdapter : BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_fate_person) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.fateIcon)
        if (item.checked) {
            helper.itemView.fateStatus.setImageResource(R.drawable.icon_checked_orange)
        } else {
            helper.itemView.fateStatus.setImageResource(R.drawable.icon_uncheck_gray)
        }
    }
}