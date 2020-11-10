package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ContactBean
import kotlinx.android.synthetic.main.item_contact_book.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/614:30
 *    desc   :
 *    version: 1.0
 */
class ContactStarAdapter(var star: Boolean = true) :
    BaseQuickAdapter<ContactBean, BaseViewHolder>(R.layout.item_contact_book) {
    override fun convert(helper: BaseViewHolder, item: ContactBean) {
        val position = helper.layoutPosition
        if (position == 0 && star) {
            helper.itemView.tv_index.visibility = View.VISIBLE
            helper.itemView.tv_index.text = mContext.getString(R.string.star_friend)
        } else {
            helper.itemView.tv_index.visibility = View.GONE
        }

        GlideUtil.loadCircleImg(mContext, item.avatar ?: "", helper.itemView.friendIcon)
        helper.itemView.friendName.text = item.nickname
    }

}