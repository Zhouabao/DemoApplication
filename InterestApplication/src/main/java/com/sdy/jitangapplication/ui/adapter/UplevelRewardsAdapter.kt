package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.item_uplevel_rewards.view.*

/**
 *    author : ZFM
 *    date   : 2020/6/817:15
 *    升级奖励
 *    version: 1.0
 */
class UplevelRewardsAdapter() :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_uplevel_rewards) {
    override fun convert(helper: BaseViewHolder, item: GiftBean) {
        if (item.checked) {
            helper.itemView.uplevelRewardsCl.setBackgroundResource(R.drawable.shape_rectangle_gray_white_10dp)
            helper.itemView.uplevel.setBackgroundResource(R.drawable.rectangle_gray_white_12dp)
            helper.itemView.uplevelRewardsMoney.setBackgroundResource(R.drawable.rectangle_gray_top_10dp)
            helper.itemView.uplevelRewardsMoney.setTextColor(Color.parseColor("#FF888D92"))
            helper.itemView.uplevelRewardsState.setTextColor(Color.parseColor("#FF888D92"))
        } else {
            helper.itemView.uplevelRewardsCl.setBackgroundResource(R.drawable.shape_rectangle_orange_white_10dp)
            helper.itemView.uplevel.setBackgroundResource(R.drawable.rectangle_orange_white_12dp)
            helper.itemView.uplevelRewardsMoney.setBackgroundResource(R.drawable.rectangle_orange_top_10dp)
            helper.itemView.uplevelRewardsMoney.setTextColor(Color.WHITE)
            helper.itemView.uplevelRewardsState.setTextColor(Color.parseColor("#ff6318"))
        }
    }
}