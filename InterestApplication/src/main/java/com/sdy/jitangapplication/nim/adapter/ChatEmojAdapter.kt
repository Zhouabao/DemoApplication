package com.sdy.jitangapplication.nim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.business.session.emoji.EmojiManager
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_chat_emoj.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/110:52
 *    desc   :
 *    version: 1.0
 */
class ChatEmojAdapter : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_chat_emoj) {
    override fun convert(helper: BaseViewHolder, item: Int) {

        helper.itemView.chatEmoj.setImageDrawable(
            EmojiManager.getDisplayDrawable(
                mContext,
                helper.layoutPosition
            )
        )

    }

}