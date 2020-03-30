package com.sdy.jitangapplication.ui.holder

import android.view.View
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.item_product_detail_img.view.*

/**
 * 推荐的banner商品
 */
class ProductDetailImgHolderView : ViewHolder<String> {
    override fun getLayoutId(): Int {
        return R.layout.item_product_detail_img
    }

    override fun onBind(itemView: View, data: String, position: Int, size: Int) {
        GlideUtil.loadImg(itemView.context, data, itemView.ivProduct)
    }
}