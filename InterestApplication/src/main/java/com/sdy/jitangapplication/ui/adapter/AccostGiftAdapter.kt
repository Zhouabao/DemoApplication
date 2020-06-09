package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.item_accost_gift.view.*

/**
 *    author : ZFM
 *    date   : 2020/6/817:15
 *    desc   :搭讪礼物列表适配器
 *    from :1 今日缘分 2赠送搭讪礼物
 *    version: 1.0
 */
class AccostGiftAdapter() :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_accost_gift) {
    override fun convert(helper: BaseViewHolder, item: GiftBean) {
        GlideUtil.loadImg(mContext, item.icon, helper.itemView.giftImg)
//        GlideUtil.loadImg(mContext, UserManager.getAvator(), helper.itemView.giftImg)
        helper.itemView.giftName.text = item.title
        helper.itemView.giftPrice.text = CommonFunction.num2thousand("${item.amount}")

        if (item.checked) {
            helper.itemView.checkedBg.visibility = View.VISIBLE
        } else
            helper.itemView.checkedBg.visibility = View.INVISIBLE
    }
}