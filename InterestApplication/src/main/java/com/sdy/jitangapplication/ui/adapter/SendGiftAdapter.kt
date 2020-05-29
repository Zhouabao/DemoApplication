package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.item_send_gift.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/210:16
 *    desc   :
 *    version: 1.0
 */
class SendGiftAdapter(val fromWantBeFriend: Boolean = false) :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_send_gift) {
    override fun convert(helper: BaseViewHolder, item: GiftBean) {
        GlideUtil.loadImg(mContext, item.icon, helper.itemView.giftImg)
        helper.itemView.giftName.text = item.title
        helper.itemView.giftPrice.text = CommonFunction.num2thousand("${item.amount}")

        if (item.checked) {
            helper.itemView.checkedBg.visibility = View.VISIBLE
        } else
            helper.itemView.checkedBg.visibility = View.INVISIBLE
    }

}