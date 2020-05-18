package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.WantFriendBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import kotlinx.android.synthetic.main.item_want_product.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2517:43
 *    desc   :想要商品
 *    version: 1.0
 */
class WantProductAdapter :
    BaseQuickAdapter<WantFriendBean, BaseViewHolder>(R.layout.item_want_product) {
    override fun convert(helper: BaseViewHolder, item: WantFriendBean) {
        helper.addOnClickListener(R.id.donate)
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.wantAvator)
        helper.itemView.wantName.text = item.nickname
        helper.itemView.wantRelationship.isVisible = !item.ship_str.isNullOrEmpty()
        helper.itemView.wantRelationship.text = item.ship_str
        helper.itemView.clickWithTrigger {
            MatchDetailActivity.start(mContext, item.accid)
        }
    }
}