package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_square_label.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/2515:14
 *    desc   :
 *    version: 1.0
 */
class SquareTagAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_square_label) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.squareTag.text = item
    }
}