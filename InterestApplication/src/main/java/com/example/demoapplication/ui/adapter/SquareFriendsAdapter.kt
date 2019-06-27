package com.example.demoapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean

/**
 *    author : ZFM
 *    date   : 2019/6/2713:39
 *    desc   :
 *    version: 1.0
 */
class SquareFriendsAdapter(datas: MutableList<MatchBean>) :
    BaseQuickAdapter<MatchBean, BaseViewHolder>(R.layout.item_square_friends, datas) {

    override fun convert(helper: BaseViewHolder, item: MatchBean) {
        helper.setText(R.id.friendName, item.name)

        GlideUtil.loadImg(mContext, item.imgs[0], helper.getView(R.id.friendsIv))
    }
}