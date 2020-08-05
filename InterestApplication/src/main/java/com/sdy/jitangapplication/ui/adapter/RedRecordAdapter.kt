package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.RedRecordBean
import kotlinx.android.synthetic.main.item_red_record.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/259:57
 *    desc   :流水记录
 *    version: 1.0
 */
class RedRecordAdapter :
    BaseQuickAdapter<RedRecordBean, BaseViewHolder>(R.layout.item_red_record) {
    override fun convert(helper: BaseViewHolder, item: RedRecordBean) {
        helper.itemView.recordTime.text = item.create_time

        helper.itemView.recordMoney.text = "提出 ${item.amount} 元"
        helper.itemView.recordId.text = "提现ID:${item.trade_no}"
        when (item.status) {
            1 -> {
                helper.itemView.recordStatus.text = "审核中"
                helper.itemView.recordStatus.setTextColor(Color.parseColor("#FF191919"))
            }
            2 -> {
                helper.itemView.recordStatus.text = "已完成"
                helper.itemView.recordStatus.setTextColor(Color.parseColor("#FFC5C6C8"))
            }
            else -> {
                helper.itemView.recordStatus.text = "已拒绝"
                helper.itemView.recordStatus.setTextColor(Color.parseColor("#FFFD4417"))
            }
        }
    }
}