package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.FriendBean

/**
 *    author : ZFM
 *    date   : 2019/6/2713:39
 *    desc   :
 *    version: 1.0
 */
class SquareFriendsAdapter(datas: MutableList<FriendBean>) :
    BaseQuickAdapter<FriendBean, BaseViewHolder>(R.layout.item_square_friends, datas) {

    override fun convert(helper: BaseViewHolder, item: FriendBean) {
        helper.setText(R.id.friendName, item.nickname.toString())

        GlideUtil.loadAvatorImg(mContext, item.avatar, helper.getView(R.id.friendsIv))
    }
}