package com.sdy.jitangapplication.ui.holder

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.BannerProductBean
import kotlinx.android.synthetic.main.item_recommend_product.view.*

class RecommendProductAdapter :
    BaseQuickAdapter<BannerProductBean, BaseViewHolder>(R.layout.item_recommend_product) {


    override fun convert(helper: BaseViewHolder, item: BannerProductBean) {
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
        helper.itemView.productName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.productImg,
            SizeUtils.dp2px(10F)
        )
    }


}