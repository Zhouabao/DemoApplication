package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyInvitedBean
import kotlinx.android.synthetic.main.item_my_invited.view.*

/**
 *    author : ZFM
 *    date   : 2020/6/817:15
 *    desc   :我的邀请记录
 *    version: 1.0
 */
class MyInvitedAdapter(val from: Int = FROM_INVITED) :
    BaseQuickAdapter<MyInvitedBean, BaseViewHolder>(R.layout.item_my_invited) {
    companion object {
        const val FROM_INVITED = 0
        const val FROM_REWARDS = 1
    }

    override fun convert(helper: BaseViewHolder, item: MyInvitedBean) {
        GlideUtil.loadImg(mContext, item.avatar, helper.itemView.myInvitedAvator)
        if (from == FROM_INVITED) {

            helper.itemView.myInvitedPhone.setTextColor(Color.parseColor("#ff191919"))
            helper.itemView.myInvitedPhone.textSize = 16F
            helper.itemView.myInvitedPayState.setTextColor(Color.parseColor("#ffff6318"))
            helper.itemView.myInvitedPayState.textSize = 12F

            if (item.is_payed) {
                helper.itemView.myInvitedPayState.text = "已付费"
                helper.itemView.myInvitedPayState.setTextColor(Color.parseColor("#FFFF6318"))
            } else {
                helper.itemView.myInvitedPayState.text = "未付费"
                helper.itemView.myInvitedPayState.setTextColor(Color.parseColor("#FFC5C6C8"))
            }
            helper.itemView.myInvitedName.text = item.nickname
            helper.itemView.myInvitedPhone.text = item.account


        } else {
            helper.itemView.myInvitedPhone.setTextColor(Color.parseColor("#FF888D92"))
            helper.itemView.myInvitedPhone.textSize = 12F
            helper.itemView.myInvitedPayState.setTextColor(Color.parseColor("#FFFD4417"))
            helper.itemView.myInvitedPayState.textSize = 16F

            helper.itemView.myInvitedName.text = item.account
            helper.itemView.myInvitedPhone.text = item.descr
            helper.itemView.myInvitedPayState.text = if (item.amount > 0) {
                "+${item.amount}"
            } else {
                "-${item.amount}"
            }


        }


    }
}