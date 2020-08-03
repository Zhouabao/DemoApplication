package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.Level
import kotlinx.android.synthetic.main.item_uplevel_rewards.view.*
import java.math.BigDecimal

/**
 *    author : ZFM
 *    date   : 2020/6/817:15
 *    升级奖励
 *    version: 1.0
 */
class UplevelRewardsAdapter() :
    BaseQuickAdapter<Level, BaseViewHolder>(R.layout.item_uplevel_rewards) {
    override fun convert(helper: BaseViewHolder, item: Level) {
        if (item.isget) {
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

        helper.itemView.uplevel.text = item.title

        SpanUtils.with(helper.itemView.uplevelRewardsMoney)
            .append("¥")
            .setFontSize(16, true)
            .append(
                "${BigDecimal(item.reward_money).setScale(
                    0,
                    BigDecimal.ROUND_HALF_UP
                )}"
            )
            .setFontSize(36, true)
            .create()

        helper.itemView.uplevelRewardsState.text = if (item.isget) {
            "已领取"
        } else {
            "邀请${item.set_cnt}人"
        }

    }
}