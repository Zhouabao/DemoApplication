package com.sdy.jitangapplication.ui.holder

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.BannerProductBean
import com.sdy.jitangapplication.ui.activity.CandyProductDetailActivity
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.item_recommend_product_banner.view.*
import org.jetbrains.anko.startActivity

/**
 * 推荐的banner商品
 */
class RecommendBannerHolderView : ViewHolder<MutableList<BannerProductBean>> {
    override fun getLayoutId(): Int {
        return R.layout.item_recommend_product_banner
    }

    override fun onBind(
        itemView: View,
        data: MutableList<BannerProductBean>,
        position: Int,
        size: Int
    ) {
        itemView.rvBannerProduct.layoutManager = GridLayoutManager(itemView.context, 3)
        val adapter = RecommendProductAdapter()
        itemView.rvBannerProduct.adapter = adapter
        adapter.addData(data)
        adapter.setOnItemClickListener { _, view, position ->
            itemView.context.startActivity<CandyProductDetailActivity>("id" to adapter.data[position].id)
        }
    }
}