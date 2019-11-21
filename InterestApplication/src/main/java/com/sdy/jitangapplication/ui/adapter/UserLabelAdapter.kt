package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelBean
import kotlinx.android.synthetic.main.item_label_all.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   : 个人中心标签适配器
 *    version: 1.0
 */
class UserLabelAdapter : BaseQuickAdapter<LabelBean, BaseViewHolder>(R.layout.item_label_all) {
    override fun convert(helper: BaseViewHolder, item: LabelBean) {
        val params = helper.itemView.labelCover.layoutParams as ConstraintLayout.LayoutParams
        params.width = SizeUtils.dp2px(119f)
        params.height = SizeUtils.dp2px(70f)
        helper.itemView.labelCover.layoutParams = params



        helper.itemView.labelName.text = item.title
        helper.itemView.labelChoose.isVisible = false
        helper.itemView.labelCoverBg.isVisible = false
        helper.itemView.labelCoverBg1.alpha = 0.6F
//        GlideUtil.loadRoundImgCenterCrop(
//            mContext,
//            item.icon,
//            helper.itemView.labelCover,
//            SizeUtils.dp2px(5F)
//        )
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            R.drawable.icon_logo,
            helper.itemView.labelCover,
            SizeUtils.dp2px(5F)
        )
    }
}