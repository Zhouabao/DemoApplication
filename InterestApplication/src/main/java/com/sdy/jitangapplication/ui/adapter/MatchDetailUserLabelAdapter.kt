package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import kotlinx.android.synthetic.main.item_layout_match_detail_user.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/2816:26
 *    desc   :
 *    version: 1.0
 */
class MatchDetailUserLabelAdapter :
    BaseQuickAdapter<MyLabelBean, BaseViewHolder>(R.layout.item_layout_match_detail_user) {
    override fun convert(helper: BaseViewHolder, item: MyLabelBean) {
        helper.itemView.labelName.text = item.title
        helper.itemView.labelIntroduce.setContent(item.describle)
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon, SizeUtils.dp2px(12F))
        helper.itemView.labelSameCount.isVisible = item.same_quality_count > 0
        helper.itemView.labelSameCount.text = "你们有${item.same_quality_count}个重合兴趣特质"
        if (item.same_label) {
            val left = mContext.resources.getDrawable(R.drawable.icon_cuohe_tag)
            helper.itemView.labelName.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null)
        } else {
            helper.itemView.labelName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }

        helper.itemView.labelAim.isVisible = item.intention.isNotEmpty()
        if (item.intention.isNotEmpty()) {
            helper.itemView.labelAim.text = item.intention[0].content
        }
        val labelQualityMyAdapter = LabelQualityAdapter()
        val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        helper.itemView.labelQualityRv.layoutManager = manager
        helper.itemView.labelQualityRv.adapter = labelQualityMyAdapter
        labelQualityMyAdapter.setNewData(item.label_quality)
    }
}