package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_greet_user_chat_content.view.*

class ChatContentAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_greet_user_chat_content) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.itemView.chatContentMsg.text = item

    }

}
