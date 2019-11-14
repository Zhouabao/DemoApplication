package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.DetailUserInfoBean
import kotlinx.android.synthetic.main.item_user_information.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/1413:44
 *    desc   :
 *    version: 1.0
 */
class MatchDetailInfoAdapter : BaseQuickAdapter<DetailUserInfoBean, BaseViewHolder>(R.layout.item_user_information) {

    override fun convert(helper: BaseViewHolder, item: DetailUserInfoBean) {
        helper.itemView.infoIv.setImageResource(item.icon)
        helper.itemView.infoTitle.text = item.title
        helper.itemView.infoContent.text = item.content
    }
}