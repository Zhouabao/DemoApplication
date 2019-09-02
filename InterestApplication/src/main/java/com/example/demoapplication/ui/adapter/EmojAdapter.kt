package com.example.demoapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
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
        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F)) / 8f).toInt()
        params.height = (SizeUtils.dp2px(250F) / 5F).toInt()
        helper.itemView.layoutParams = params
        helper.itemView.emojTv.text = item
    }
}