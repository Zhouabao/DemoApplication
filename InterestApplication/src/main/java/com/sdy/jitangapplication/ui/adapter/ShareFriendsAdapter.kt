package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_share_friends.view.*


/**
 *    author : ZFM
 *    date   : 2020/7/110:28
 *    desc   :
 *    version: 1.0
 */
class ShareFriendsAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_share_friends) {
    override fun convert(helper: BaseViewHolder, item: String) {

        if (item.isNullOrEmpty()) {
            GlideUtil.loadCircleImg(
                mContext,
                R.drawable.icon_share_friends_default,
                helper.itemView.shareFriendsAvator
            )
            helper.itemView.shareFriendsName.text = "邀请助力"
        }
    }

}