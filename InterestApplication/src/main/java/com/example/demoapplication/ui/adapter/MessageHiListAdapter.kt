package com.example.demoapplication.ui.adapter

import android.os.CountDownTimer
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.event.UpdateHiEvent
import com.example.demoapplication.model.HiMessageBean
import kotlinx.android.synthetic.main.item_message_hi_list.view.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/8/511:22
 *    desc   : 招呼列表adapter
 *    version: 1.0
 */
class MessageHiListAdapter : BaseQuickAdapter<HiMessageBean, BaseViewHolder>(R.layout.item_message_hi_list) {

    override fun convert(holder: BaseViewHolder, item: HiMessageBean) {
        val itemView = holder.itemView
        GlideUtil.loadAvatorImg(mContext, item.avatar, holder.itemView.msgIcon)
        holder.itemView.msgTitle.text = item.nickname ?: ""
//        holder.itemView.text.text = item.content ?: ""
        holder.itemView.latelyTime.text = item.create_time ?: ""
        if (item.count != null && item.count > 0) {
            holder.itemView.newCount.text = "${item.count}"
            holder.itemView.newCount.visibility = View.VISIBLE
        } else {
            holder.itemView.newCount.visibility = View.GONE
        }
        holder.itemView.text.text = item.content ?: ""

        // *    1，新消息 2，倒计时 3，普通样式 4 过期
        when (item.type) {
            1 -> {
                itemView.msgNofitySensor.visibility = View.VISIBLE
                itemView.msgCountDown.visibility = View.GONE
                itemView.msgIconMask.visibility = View.GONE
                itemView.msgTextTimer.visibility = View.GONE
                itemView.msgOuttimeTip.visibility = View.GONE
            }
            2 -> {
                itemView.msgNofitySensor.visibility = View.GONE
                itemView.msgIconMask.visibility = View.GONE
                itemView.msgCountDown.visibility = View.VISIBLE
                itemView.msgOuttimeTip.visibility = View.GONE
                itemView.msgTextTimer.visibility = View.VISIBLE
                itemView.msgTextTimer.startTime((item.countdown ?: 0).toLong(), "1")
                itemView.msgCountDown.setMaxStepNum(item.countdown_total ?: 0)
                if (item.countdown_total != null && item.countdown_total > 0) {
//                    itemView.msgCountDown.update((item.countdown_total - (item.countdown ?: 0)).toLong(), 100)
                    object : CountDownTimer((item.countdown ?: 0) * 1000L, 1000) {
                        override fun onFinish() {
                            EventBus.getDefault().post(UpdateHiEvent())
                        }

                        override fun onTick(p0: Long) {
                            item.timer++
                            itemView.msgCountDown.update(
                                (item.countdown_total - (item.countdown ?: 0) + item.timer).toLong(), 100
                            )
                        }

                    }.start()
                }
            }
            3 -> {
                itemView.msgNofitySensor.visibility = View.GONE
                itemView.msgCountDown.visibility = View.GONE
                itemView.msgIconMask.visibility = View.GONE
                itemView.msgOuttimeTip.visibility = View.GONE
                itemView.msgTextTimer.visibility = View.GONE
            }
            4 -> {
                itemView.msgNofitySensor.visibility = View.GONE
                itemView.msgCountDown.visibility = View.GONE
                itemView.msgTextTimer.visibility = View.GONE
                itemView.msgOuttimeTip.visibility = View.VISIBLE
                itemView.msgIconMask.visibility = View.VISIBLE
                itemView.msgOuttimeTip.text = "已超时"
            }

        }


    }
}