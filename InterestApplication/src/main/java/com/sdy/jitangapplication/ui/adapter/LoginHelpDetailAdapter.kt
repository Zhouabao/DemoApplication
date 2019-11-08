package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_login_help.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/815:51
 *    desc   :
 *    version: 1.0
 */
class LoginHelpDetailAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_login_help_detail) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.loginHelpTitle.text = item
    }
}