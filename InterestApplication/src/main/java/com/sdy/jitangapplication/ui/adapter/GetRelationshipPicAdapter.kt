package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyTapsBean
import kotlinx.android.synthetic.main.item_get_relationship_pic.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/714:45
 *    desc   :
 *    version: 1.0
 */
class GetRelationshipPicAdapter :
    BaseQuickAdapter<MyTapsBean, BaseViewHolder>(R.layout.item_get_relationship_pic) {
    override fun convert(helper: BaseViewHolder, item: MyTapsBean) {
        val params = helper.itemView.relationIv.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(50F)
        params.height = ((130 / 325F) * params.width * 3 / mData.size).toInt()
        helper.itemView.relationIv.layoutParams = params

        helper.itemView.relationChecked.isVisible = item.checked
        helper.itemView.relationCheckedCover.isVisible = item.checked
        helper.itemView.relationTitle.text = item.title
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.relationIv,
            SizeUtils.dp2px(10F)
        )

        helper.itemView.relationCheckedCover.setImageResource(R.drawable.shape_rectangle_halfblack_10dp)

    }
}