package com.sdy.jitangapplication.ui.holder

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.sdy.jitangapplication.R
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.item_recommend_product_banner.view.*

class RecommendBannerHolderView : ViewHolder<String> {
    override fun getLayoutId(): Int {
        return R.layout.item_recommend_product_banner
    }

    override fun onBind(itemView: View, data: String, position: Int, size: Int) {
        itemView.rvBannerProduct.layoutManager = GridLayoutManager(itemView.context, 3)
        val adapter = RecommendProductAdapter()
        itemView.rvBannerProduct.adapter = adapter
        adapter.addData("")
        adapter.addData("")
        adapter.addData("")
    }
}