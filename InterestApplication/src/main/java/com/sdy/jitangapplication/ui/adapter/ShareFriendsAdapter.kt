package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.InvitedBean
import kotlinx.android.synthetic.main.item_share_friends.view.*


/**
 *    author : ZFM
 *    date   : 2020/7/110:28
 *    desc   :
 *    version: 1.0
 */
class ShareFriendsAdapter :
    BaseQuickAdapter<InvitedBean, BaseViewHolder>(R.layout.item_share_friends) {
    override fun convert(helper: BaseViewHolder, item: InvitedBean) {

        if (item.nickname.isNullOrEmpty()) {
            GlideUtil.loadCircleImg(
                mContext,
                R.drawable.icon_share_friends_default,
                helper.itemView.shareFriendsAvator
            )
            helper.itemView.shareFriendsName.text = "邀请助力"
        } else {
            GlideUtil.loadCircleImg(
                mContext,
                item.avatar,
                helper.itemView.shareFriendsAvator
            )
            helper.itemView.shareFriendsName.text = item.nickname
        }
    }

}