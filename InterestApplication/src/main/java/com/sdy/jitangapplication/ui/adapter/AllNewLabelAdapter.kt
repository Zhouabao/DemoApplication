package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.item_label_all.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 所有新兴趣的数据
 *    version: 1.0
 */
class AllNewLabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label_all) {
    override fun convert(helper: BaseViewHolder, item: NewLabel) {

        helper.itemView.labelName.text = item.title
        helper.itemView.labelChoose.isVisible = item.checked
        helper.itemView.labelCoverBg.isVisible = item.checked
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.labelCover,
            SizeUtils.dp2px(5F)
        )
    }
}