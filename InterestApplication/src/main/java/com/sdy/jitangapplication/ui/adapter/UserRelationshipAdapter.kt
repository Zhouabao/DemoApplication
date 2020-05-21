package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_user_relationship.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class UserRelationshipAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_user_relationship) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.relationshipContent.text = item

    }
}