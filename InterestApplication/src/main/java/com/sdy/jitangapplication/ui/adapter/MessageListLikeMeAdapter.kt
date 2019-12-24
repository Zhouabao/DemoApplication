package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.HiMessageBean
import kotlinx.android.synthetic.main.item_message_likelist.view.*


/**
 *    author : ZFM
 *    date   : 2019/8/513:56
 *    desc   :
 *    普通样式 1.送出的招呼后的无回复样式
 *    过期样式 我发送/接收到的消息超时均显示灰色遮罩且无法回复，点击回复框显示「消息已过期」
 *    新消息   有新消息时波纹动效提示对方新消息
 *    计时样式 被回复方收到消息已读后进入计时样式，回复后计时样式停止并不会再次进入计时
 *    1，新消息 2，倒计时 3，普通样式 4 过期
 *    version: 1.0
 *    R.layout.item_message_friends_list_notify)
 */
class MessageListLikeMeAdapter : BaseQuickAdapter<HiMessageBean, BaseViewHolder>(R.layout.item_message_likelist) {
    override fun convert(helper: BaseViewHolder, item: HiMessageBean) {
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.likeMeAvator)
//        helper.itemView.likeMeAvatorBtn
    }


}