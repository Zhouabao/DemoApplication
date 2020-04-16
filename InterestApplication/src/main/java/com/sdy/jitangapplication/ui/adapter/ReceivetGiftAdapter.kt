package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.item_receive_gift_child.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/210:16
 *    desc   :
 *    version: 1.0
 */
class ReceivetGiftAdapter :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_receive_gift_child) {
    override fun convert(helper: BaseViewHolder, item: GiftBean) {
        GlideUtil.loadImg(mContext, item.icon, helper.itemView.giftImg)
        helper.itemView.giftName.text = item.title
        helper.itemView.giftCount.text = "x${item.cnt}"
    }

}