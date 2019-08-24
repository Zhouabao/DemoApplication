package com.example.demoapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.BlackBean
import kotlinx.android.synthetic.main.item_contact_book.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/614:30
 *    desc   :
 *    version: 1.0
 */
class BlackListAdapter : BaseQuickAdapter<BlackBean, BaseViewHolder>(R.layout.item_black_list) {
    override fun convert(helper: BaseViewHolder, item: BlackBean) {
        val position = helper.layoutPosition

        GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", helper.itemView.friendIcon)
        helper.itemView.friendName.text = "${item.nickname}"
    }

}