package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.BannerProductBean
import com.sdy.jitangapplication.model.GoodsListBean
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
    OnLoadMoreListener, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candy_mall)
        initView()
        mPresenter.goodsList()
    }

    private val candyProductAdapter by lazy { CandyProductAdapter() }
    private val productMenuAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(this) }

    private fun initView() {
        BarUtils.setStatusBarVisibility(this, false)
        btnBack.onClick {
            finish()
        }
        myOrderBtn.setOnClickListener(this)

        stateProduct.retryBtn.onClick {
            stateProduct.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.goodsList()
        }
        refreshProduct.setOnRefreshListener(this)
        refreshProduct.setOnLoadMoreListener(this)
        rvCategoryProduct.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvCategoryProduct.adapter = candyProductAdapter
        candyProductAdapter.setOnItemClickListener { _, view, position ->
            startActivity<CandyProductDetailActivity>()
        }

        (bannerRecommendProduct as BannerViewPager<MutableList<BannerProductBean>, RecommendBannerHolderView>)
            .setHolderCreator { RecommendBannerHolderView() }
            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
            .setIndicatorHeight(SizeUtils.dp2px(6f))
            .setOnPageClickListener {
            }
            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
            .create(mutableListOf())

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
            candyProductAdapter.addData("")
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.myOrderBtn -> {
                startActivity<MyOrderActivity>()
            }
        }
    }

    override fun onGoodsListResult(goodsListBean: GoodsListBean?) {
        if (goodsListBean != null) {
            for (data in goodsListBean.list.withIndex()) {
                data.value.checked = data.index == 0
            }
            productMenuAdapter.addData(goodsListBean.list)
            (bannerRecommendProduct as BannerViewPager<MutableList<BannerProductBean>, RecommendBannerHolderView>).create(
                goodsListBean.banner
            )

            stateProduct.viewState = MultiStateView.VIEW_STATE_CONTENT
        } else {
            stateProduct.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }
}
