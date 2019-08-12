package com.example.demoapplication.ui.adapter

import android.os.CountDownTimer
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.item_message_friends_list.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/513:56
 *    desc   :
 *    version: 1.0
 */
class MessageListFriensAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_message_friends_list) {
    var time = 0
    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.itemView.countTimer.setMaxStepNum(60)
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {

            }

            override fun onTick(p0: Long) {
                time++
                helper.itemView.countTimer.update(time.toLong(), 100)
            }

        }.start()
    }
}