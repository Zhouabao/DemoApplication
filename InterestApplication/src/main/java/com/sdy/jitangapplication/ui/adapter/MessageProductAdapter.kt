package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ProductMsgBean
import kotlinx.android.synthetic.main.item_message.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2517:43
 *    desc   : 商品留言
 *    version: 1.0
 */
class MessageProductAdapter : BaseQuickAdapter<ProductMsgBean, BaseViewHolder>(R.layout.item_message) {
    override fun convert(helper: BaseViewHolder, item: ProductMsgBean) {
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.messageAvator)
        helper.addOnClickListener(R.id.messageAvator)
        helper.itemView.messageName.text = item.nickname
        helper.itemView.messageContent.text = item.content
        helper.itemView.messgaeTime.text = item.create_time
    }
}