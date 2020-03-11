package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareBean
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_message_square_list.view.squareContent
import kotlinx.android.synthetic.main.item_message_square_list.view.squareImg
import kotlinx.android.synthetic.main.item_recommend_square.view.*

class RecommendSquareAdapter : BaseQuickAdapter<SquareBean, BaseViewHolder>(R.layout.item_recommend_square) {
    override fun convert(helper: BaseViewHolder, item: SquareBean) {
        val params = helper.itemView.squareImg.layoutParams as ConstraintLayout.LayoutParams
        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(37F)) / 2
        params.height  = params.width
        helper.itemView.squareImg.layoutParams = params
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.avatar,
            helper.itemView.squareImg,
            SizeUtils.dp2px(10F),
            RoundedCornersTransformation.CornerType.TOP
        )


        helper.itemView.squareDistance.text = item.distance
        helper.itemView.squareContent.text = item.descr
        helper.itemView.squareLike.text = "${item.like_cnt}"
        helper.itemView.squareName.text = item.nickname
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.squareAvator)

    }
}