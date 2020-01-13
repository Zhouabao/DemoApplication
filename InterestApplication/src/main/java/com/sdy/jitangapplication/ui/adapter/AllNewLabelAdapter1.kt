package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import kotlinx.android.synthetic.main.item_label_all1.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 所有新标签的数据
 *    version: 1.0
 */
class AllNewLabelAdapter1(var index: Int = 1, var from: Int = AddLabelActivity.FROM_ADD_NEW) :
    BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label_all1) {
    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        //state: Int = 0 //0 没有使用过 1正在使用中 2使用过的
        if (from == AddLabelActivity.FROM_REGISTER || from == AddLabelActivity.FROM_INTERSERT_LABEL) {
            helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_interest_added)
        } else {
            helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_added)
        }

        helper.itemView.labelAdded.isVisible = item.checked
        if (index == 0) {
            val params = helper.itemView.labelRoot.layoutParams as RecyclerView.LayoutParams
            params.width = SizeUtils.dp2px(100F)
            params.height = SizeUtils.dp2px(100F)
            helper.itemView.layoutParams = params
        } else {
            val params = helper.itemView.labelRoot.layoutParams as RecyclerView.LayoutParams
            params.width = SizeUtils.dp2px(107F)
            params.height = SizeUtils.dp2px(107F)
            helper.itemView.layoutParams = params
        }
        helper.itemView.labelName.text = item.title
        helper.itemView.labelPersonNum.text = "${item.used_cnt}"
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.labelImg,
            SizeUtils.dp2px(15F)
        )
    }
}