package com.example.demoapplication.ui.adapter

import android.os.CountDownTimer
import android.util.SparseArray
import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.event.UpdateHiEvent
import com.example.demoapplication.model.HiMessageBean
import kotlinx.android.synthetic.main.item_message_friends_list_countdown.view.*
import kotlinx.android.synthetic.main.item_message_friends_list_notify.view.*
import kotlinx.android.synthetic.main.item_message_friends_normal.view.*
import kotlinx.android.synthetic.main.item_message_friends_outtime.view.*
import org.greenrobot.eventbus.EventBus


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
class MessageListFriensAdapter(data: MutableList<HiMessageBean>) :
    BaseMultiItemQuickAdapter<HiMessageBean, MessageListFriensAdapter.MyViewHolder>(data) {
    private var coundownMap: SparseArray<CountDownTimer> = SparseArray()

    /**
     *  清空资源
     */
    public fun cancelAllTimers() {
        for (i in 0 until coundownMap.size()) {
            val cdt = coundownMap[coundownMap.keyAt(i)]
            if (cdt != null) {
                cdt.cancel()
            }
        }
    }

    init {
        addItemType(1, R.layout.item_message_friends_list_notify)
        addItemType(2, R.layout.item_message_friends_list_countdown)
        addItemType(3, R.layout.item_message_friends_normal)
        addItemType(4, R.layout.item_message_friends_outtime)
    }

    override fun convert(holder: MyViewHolder, item: HiMessageBean) {
        // *    1，新消息 2，倒计时 3，普通样式 4 过期
        when (holder.itemViewType) {
            1 -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.nofityAvator)
            }
            2 -> {
                if (holder.countDownTimer != null) {
                    holder.countDownTimer!!.cancel()
                }
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.countAvator)
                holder.itemView.countTimer.setMaxStepNum(item.countdown_total ?: 0)
                if (item.countdown_total != null && item.countdown_total > 0) {
                    holder.itemView.countTimer.update((item.countdown_total - (item.countdown ?: 0)).toLong(), 100)
                    holder.countDownTimer = object : CountDownTimer((item.countdown ?: 0).toLong() * 1000, 1000) {
                        override fun onFinish() {
                            EventBus.getDefault().post(UpdateHiEvent())
                        }

                        override fun onTick(p0: Long) {
                            item.timer++
                            holder.itemView.countTimer.update(
                                (item.countdown_total - (item.countdown ?: 0) + item.timer).toLong(), 100
                            )
                        }

                    }.start()
                    coundownMap.put(holder.itemView.hashCode(), holder.countDownTimer)
                }

            }
            3 -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.normalAvator)
            }
            else -> {
                GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.outtimeAvator)
            }

        }

    }


    public class MyViewHolder(view: View) : BaseViewHolder(view) {
        public var countDownTimer: CountDownTimer? = null

    }

}