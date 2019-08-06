package com.example.demoapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.ContactBean
import kotlinx.android.synthetic.main.item_contact_book.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/614:30
 *    desc   :
 *    version: 1.0
 */
class ContactAdapter : BaseQuickAdapter<ContactBean, BaseViewHolder>(R.layout.item_contact_book) {
    override fun convert(helper: BaseViewHolder, item: ContactBean) {
        val position = helper.layoutPosition
        //因为添加了头部 所以位置要移动
        if ((position == 1 || data[position - 1 - 1].index != item.index)) {
            helper.itemView.tv_index.visibility = View.VISIBLE
            helper.itemView.tv_index.text = item.index
            helper.itemView.friendDivider.visibility = View.GONE
        } else {
            helper.itemView.tv_index.visibility = View.GONE
            helper.itemView.friendDivider.visibility = View.VISIBLE

        }

        GlideUtil.loadCircleImg(mContext, item.avatar ?: "", helper.itemView.friendIcon)
        helper.itemView.friendName.text = "${item.nickname}"
    }

}