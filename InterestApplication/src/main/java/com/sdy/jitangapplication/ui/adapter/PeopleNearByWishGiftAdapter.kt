package com.sdy.jitangapplication.ui.adapter

import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import kotlinx.android.synthetic.main.item_people_nearby_wish_gift.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2411:16
 *    desc   : 心愿礼物适配器
 *    version: 1.0
 */
class PeopleNearByWishGiftAdapter(val accid: String) :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_people_nearby_wish_gift) {

    override fun convert(helper: BaseViewHolder, item: GiftBean) {

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.giftImg,
            SizeUtils.dp2px(10F)
        )

        helper.itemView.giftDesc.text = "${item.title}"
        helper.addOnClickListener(R.id.helpWishBtn)

        helper.itemView.clickWithTrigger {
            MatchDetailActivity.start(mContext, accid)
        }

    }

}