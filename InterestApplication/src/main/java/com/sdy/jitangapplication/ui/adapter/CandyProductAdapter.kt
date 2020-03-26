package com.sdy.jitangapplication.ui.adapter

import android.graphics.Typeface
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_candy_product.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2411:16
 *    desc   :糖果商品
 *    version: 1.0
 */
class CandyProductAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_candy_product) {
    override fun convert(helper: BaseViewHolder, item: String) {

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            R.drawable.icon_bg_pic_my_candy,
            helper.itemView.productImg,
            SizeUtils.dp2px(10F),
            RoundedCornersTransformation.CornerType.LEFT
        )
        helper.itemView.ProductCandyPrice.typeface = Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
    }
}