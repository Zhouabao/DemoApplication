package com.example.demoapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.item_emoj.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/29:46
 *    desc   :
 *    version: 1.0
 */
class EmojAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_emoj) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.emojTv.text = item
    }
}