package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_invite_rule.view.*

/**
 *    author : ZFM
 *    date   : 2020/6/817:15
 *    desc   :搭讪礼物列表适配器
 *    from :1 今日缘分 2赠送搭讪礼物
 *    version: 1.0
 */
class InviteRuleAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_invite_rule) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.rule11.text = item
        helper.itemView.rule1.text = "${helper.layoutPosition + 1}"


    }
}