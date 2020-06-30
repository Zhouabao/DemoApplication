package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.UserRelationshipBean
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_user_intention.view.*
import kotlinx.android.synthetic.main.item_user_relationship.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class UserRelationshipAdapter :
    BaseMultiItemQuickAdapter<UserRelationshipBean, BaseViewHolder>(mutableListOf()) {
    init {
        addItemType(0, R.layout.item_user_intention)
        addItemType(1, R.layout.item_user_relationship)
    }


    override fun convert(helper: BaseViewHolder, item: UserRelationshipBean) {
        when (helper.itemViewType) {
            0 -> {
                helper.itemView.userIntentionContent.text = item.title
            }
            1 -> {
                helper.itemView.relationshipContent.text = item.title
                helper.itemView.setBackgroundResource(
                    if (UserManager.getGender() == 1) {
                        R.drawable.shape_rectangle_white80_14dp
                    } else {
                        R.drawable.shape_rectangle_f4_14dp
                    }
                )
            }
        }
    }
}