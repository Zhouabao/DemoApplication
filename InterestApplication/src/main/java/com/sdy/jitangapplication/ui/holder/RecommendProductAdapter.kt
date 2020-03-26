package com.sdy.jitangapplication.ui.holder

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_recommend_product.view.*

class RecommendProductAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_recommend_product) {


    override fun convert(helper: BaseViewHolder, item: String?) {
        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams

        if (helper.layoutPosition == 0) {
            params.leftMargin = SizeUtils.dp2px(10F)
        } else {
            params.leftMargin = SizeUtils.dp2px(5F)
        }
        if (helper.layoutPosition == mData.size - 1) {
            params.rightMargin = SizeUtils.dp2px(10F)
        } else {
            params.rightMargin = 0
        }
        helper.itemView.layoutParams = params

//        val params1 = helper.itemView.productImg.layoutParams as ConstraintLayout.LayoutParams
//        params1.height = params.width
//        params1.width = params.width

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            R.drawable.icon_bg_actionbar_candy_mall,
            helper.itemView.productImg,
            SizeUtils.dp2px(10F)
        )
    }


}