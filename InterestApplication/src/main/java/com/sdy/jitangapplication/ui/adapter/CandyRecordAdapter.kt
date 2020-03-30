package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.BillBean
import kotlinx.android.synthetic.main.item_record_candy.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/259:57
 *    desc   :流水记录
 *    version: 1.0
 */
class CandyRecordAdapter : BaseQuickAdapter<BillBean, BaseViewHolder>(R.layout.item_record_candy) {
    override fun convert(helper: BaseViewHolder, item: BillBean) {
        GlideUtil.loadCircleImg(mContext, item.icon, helper.itemView.recordImg)
        helper.itemView.recordTime.text = item.create_time

        if (item.affect_candy > 0) {
            helper.itemView.recordMoney.setTextColor(Color.parseColor("#FFFD4417"))
            helper.itemView.recordMoney.text = "+${item.affect_candy}"
        } else {
            helper.itemView.recordMoney.text = "${item.affect_candy}"
            helper.itemView.recordMoney.setTextColor(Color.parseColor("#FF191919"))
        }
        helper.itemView.recordContent.text = "${item.intro}"
        helper.itemView.recordType.text = "${item.type_title}"

    }
}