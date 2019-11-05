package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ReportBean
import kotlinx.android.synthetic.main.item_layout_report_reason.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/3119:57
 *    desc   : 举报理由adapter
 *    version: 1.0
 */
class ReportResonAdapter : BaseQuickAdapter<ReportBean, BaseViewHolder>(R.layout.item_layout_report_reason) {

    override fun convert(helper: BaseViewHolder, item: ReportBean) {

        helper.addOnClickListener(R.id.reportReason)
        helper.itemView.reportReason.text = item.reason
        helper.itemView.reportReasonCheck.isChecked = item.checked
    }
}