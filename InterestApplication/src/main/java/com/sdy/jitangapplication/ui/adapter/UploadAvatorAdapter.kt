package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_upload_avator.view.*

/**
 *    author : ZFM
 *    date   : 2019/9/2317:05
 *    desc   :
 *    version: 1.0
 */
class UploadAvatorAdapter : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_upload_avator) {

    override fun convert(helper: BaseViewHolder, item: Int) {
        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.height = SizeUtils.dp2px(125f)
        params.width =
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 4 - SizeUtils.dp2px(10f) * 3) / 4f).toInt()
        if (helper.layoutPosition != 0) {
            params.leftMargin = SizeUtils.dp2px(10F)
        } else {
            params.leftMargin = 0
        }
        helper.itemView.layoutParams = params
        GlideUtil.loadRoundImgCenterCrop(mContext, item, helper.itemView.ivUpload, SizeUtils.dp2px(5F))
        if (helper.layoutPosition == 0) {
            helper.itemView.ivStatu.setImageResource(R.drawable.icon_pass)
        } else {
            helper.itemView.ivStatu.setImageResource(R.drawable.icon_not_pass)
        }

    }
}