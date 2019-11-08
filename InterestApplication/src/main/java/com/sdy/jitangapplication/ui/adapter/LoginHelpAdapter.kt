package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LoginHelpBean
import kotlinx.android.synthetic.main.item_login_help.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/815:51
 *    desc   :
 *    version: 1.0
 */
class LoginHelpAdapter : BaseQuickAdapter<LoginHelpBean, BaseViewHolder>(R.layout.item_login_help) {
    override fun convert(helper: BaseViewHolder, item: LoginHelpBean) {
        helper.itemView.loginHelpTitle.text = item.title
    }
}