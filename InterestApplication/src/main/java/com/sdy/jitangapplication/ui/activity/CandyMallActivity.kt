package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.CandyMallPresenter
import com.sdy.jitangapplication.presenter.view.CandyMallView
import com.sdy.jitangapplication.ui.adapter.CandyProductAdapter
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.ui.holder.RecommendBannerHolderView
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.activity_candy_mall.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.jetbrains.anko.startActivity

/**
 * 糖果商城
 */
class CandyMallActivity : BaseMvpActivity<CandyMallPresenter>(), CandyMallView, OnRefreshListener,
    OnLoadMoreListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candy_mall)
        initView()
    }

    private val candyProductAdapter by lazy { CandyProductAdapter() }
    private val productMenuAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(this) }

    private fun initView() {
        btnBack.onClick {
            finish()
        }


        stateProduct.retryBtn.onClick {

        }

        refreshProduct.setOnRefreshListener(this)
        refreshProduct.setOnLoadMoreListener(this)
        rvCategoryProduct.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvCategoryProduct.adapter = candyProductAdapter

        candyProductAdapter.setOnItemClickListener { _, view, position ->
            startActivity<CandyProductDetailActivity>()
        }



        for (i in 0 until 10) {
            candyProductAdapter.addData("")
        }


        (bannerRecommendProduct as BannerViewPager<String, RecommendBannerHolderView>)
            .setHolderCreator { RecommendBannerHolderView() }
            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
            .setIndicatorHeight(SizeUtils.dp2px(6f))
            .setOnPageClickListener {
            }
            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
            .create(mutableListOf("", "", "", ""))

        //糖果商品种类
        rvCandyCategory.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(rvCandyCategory)
        rvCandyCategory.adapter = productMenuAdapter

        productMenuAdapter.setOnItemClickListener { _, view, position ->
            //todo 点击切换数据源
            for (data in productMenuAdapter.data.withIndex()) {
                data.value.checked = data.index == position
            }
            productMenuAdapter.notifyDataSetChanged()

            (rvCandyCategory.layoutManager as CenterLayoutManager).smoothScrollToPosition(
                rvCandyCategory,
                RecyclerView.State(),
                position
            )
        }

        for (i in 0 until 10) {
            productMenuAdapter.addData(NewLabel(title = "${i}哈哈哈哈哈哈哈", checked = i == 0))
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        candyProductAdapter.data.clear()
        candyProductAdapter.notifyDataSetChanged()
        for (i in 0 until 10) {
            candyProductAdapter.addData("")
        }
        refreshLayout.finishRefresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        for (i in 0 until 10) {
            candyProductAdapter.addData("")
        }
        refreshLayout.finishLoadMore()
    }
}
