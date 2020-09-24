package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.UserRelationshipBean
import com.sdy.jitangapplication.ui.activity.DatingDetailActivity
import kotlinx.android.synthetic.main.item_sweet_heart_verify.view.*
import kotlinx.android.synthetic.main.item_user_intention.view.*
import kotlinx.android.synthetic.main.item_user_relationship.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class UserRelationshipAdapter(val from_sweet_heart: Int = 0) :
    BaseMultiItemQuickAdapter<UserRelationshipBean, BaseViewHolder>(mutableListOf()) {
    init {
        addItemType(0, R.layout.item_user_intention)
        addItemType(1, R.layout.item_user_relationship)
        addItemType(2, R.layout.item_sweet_heart_verify)
    }


    override fun convert(helper: BaseViewHolder, item: UserRelationshipBean) {
        when (helper.itemViewType) {
            0 -> {
                helper.itemView.userIntentionContent.text = "活动：${item.title}"
                helper.itemView.userIntentionContent.clickWithTrigger {
                    DatingDetailActivity.start2Detail(mContext, item.id)
                }
            }
            1 -> {
                helper.itemView.relationshipContent.text = item.title
                if (from_sweet_heart != 0) {
                    if (from_sweet_heart == 1 || from_sweet_heart == 2 || from_sweet_heart == 5) {
                        helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_ffcd52_14dp)
                        helper.itemView.relationshipContent.setTextColor(Color.parseColor("#FF212225"))
                    } else {
                        helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_white80_14dp)
                        helper.itemView.relationshipContent.setTextColor(Color.parseColor("#FFFF7CA8"))

                    }
                } else {
                    helper.itemView.relationshipContent.setTextColor(Color.parseColor("#ff191919"))
                        helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_f4_14dp)
//                    if (UserManager.getGender() == 1) {
//                    } else {
//                        helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_white80_14dp)
//                    }
                }

            }
            2 -> {
                ////0 不是甜心圈 1 资产认证 2豪车认证 3身材 4职业  5高额充值
                helper.itemView.userSweetContent.text = item.title
                if (from_sweet_heart == 1 || from_sweet_heart == 2 || from_sweet_heart == 5) {
                    helper.itemView.userSweetBg.setImageResource(R.drawable.icon_sweet_heart_verify_man_small_bg)
                    helper.itemView.userSweetContent.setTextColor(Color.parseColor("#ffffcd52"))
                } else {
                    helper.itemView.userSweetBg.setImageResource(R.drawable.icon_sweet_heart_verify_woman_small_bg)
                    helper.itemView.userSweetContent.setTextColor(Color.WHITE)
                }
            }
        }
    }
}