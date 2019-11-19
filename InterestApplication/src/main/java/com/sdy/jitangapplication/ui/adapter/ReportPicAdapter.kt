package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_report_pic.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/114:40
 *    desc   : 举报上传的图片
 *    version: 1.0
 */
class ReportPicAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_report_pic) {

    override fun convert(helper: BaseViewHolder, item: String) {
        if (item == "") {
            helper.itemView.reportPicDelete.isVisible = false
            helper.itemView.reportPic.setImageResource(R.drawable.icon_add_report_pic)
        } else {
            helper.addOnClickListener(R.id.reportPicDelete)
            helper.itemView.reportPicDelete.isVisible = true
            GlideUtil.loadRoundImgCenterCrop(mContext, item, helper.itemView.reportPic, SizeUtils.dp2px(10F))
        }
    }
}