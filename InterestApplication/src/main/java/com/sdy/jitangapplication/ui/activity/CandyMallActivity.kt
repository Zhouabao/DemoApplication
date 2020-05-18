package com.sdy.jitangapplication.ui.activity

import android.os.Build
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
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshCandyMallEvent
import com.sdy.jitangapplication.event.UpdateWantStateEvent
import com.sdy.jitangapplication.model.BannerProductBean
import com.sdy.jitangapplication.model.GoodsCategoryBeans
import com.sdy.jitangapplication.model.GoodsListBean
import com.sdy.jitangapplication.model.ProductBean
import com.sdy.jitangapplication.presenter.CandyMallPresenter
import com.sdy.jitangapplication.presenter.view.CandyMallView
import com.sdy.jitangapplication.ui.adapter.CandyProductAdapter
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.ui.holder.RecommendBannerHolderView
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.activity_candy_mall.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

    private var checkedMenuIndex = 0
    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = CandyMallPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
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
        candyProductAdapter.setEmptyView(R.layout.empty_layout, rvCategoryProduct)
        candyProductAdapter.isUseEmpty(false)
        candyProductAdapter.setOnItemClickListener { _, view, position ->
            startActivity<CandyProductDetailActivity>("id" to candyProductAdapter.data[position].id)
        }


        (bannerRecommendProduct as BannerViewPager<MutableList<BannerProductBean>, RecommendBannerHolderView>)
            .setHolderCreator { RecommendBannerHolderView() }
            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
            .setIndicatorHeight(SizeUtils.dp2px(6f))
            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
            .create(mutableListOf())

        //糖果商品种类
        rvCandyCategory.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(rvCandyCategory)
        rvCandyCategory.adapter = productMenuAdapter

        productMenuAdapter.setOnItemClickListener { _, view, position ->
            if (checkedMenuIndex != position) {
                checkedMenuIndex = position
                for (data in productMenuAdapter.data.withIndex()) {
                    data.value.checked = data.index == position
                }
                productMenuAdapter.notifyDataSetChanged()

                refreshProduct.resetNoMoreData()
                params["category_id"] = productMenuAdapter.data[position].id
                page = 1
                params["page"] = page
                mPresenter.goodsCategoryList(params)

                (rvCandyCategory.layoutManager as CenterLayoutManager).smoothScrollToPosition(
                    rvCandyCategory,
                    RecyclerView.State(),
                    position
                )
            }
        }


    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.goodsCategoryList(params)
        refreshLayout.resetNoMoreData()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.goodsCategoryList(params)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.myOrderBtn -> {
                startActivity<MyOrderActivity>()
            }
        }
    }

    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "category_id" to -1,
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
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
            candyProductAdapter.mycandy = goodsListBean.mycandy
            params["category_id"] = goodsListBean.list[0].id
            mPresenter.goodsCategoryList(params)
            refreshProduct.resetNoMoreData()
        } else {
            stateProduct.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }

    override fun ongoodsCategoryList(goodsListBean: GoodsCategoryBeans?) {
        if (stateProduct.viewState == MultiStateView.VIEW_STATE_LOADING)
            stateProduct.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (refreshProduct.state != RefreshState.Loading) {
            rvCategoryProduct.scrollToPosition(0)
            candyProductAdapter.data.clear()
            candyProductAdapter.notifyDataSetChanged()
        }
        if (refreshProduct.state == RefreshState.Loading) {
            if ((goodsListBean?.list ?: mutableListOf<ProductBean>()).size < Constants.PAGESIZE) {
                refreshProduct.finishLoadMoreWithNoMoreData()
            } else
                refreshProduct.finishLoadMore(goodsListBean != null)
        }

        if (refreshProduct.state == RefreshState.Refreshing)
            refreshProduct.finishRefresh(goodsListBean != null)
        candyProductAdapter.addData(goodsListBean?.list ?: mutableListOf())

        if (candyProductAdapter.data.isEmpty()) {
            candyProductAdapter.isUseEmpty(true)
        } else {
            candyProductAdapter.isUseEmpty(false)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCandyMallEvent(event: RefreshCandyMallEvent) {
        refreshProduct.autoRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateWantStateEvent(event: UpdateWantStateEvent) {
        for (data in candyProductAdapter.data.withIndex()) {
            if (data.value.id == event.id) {
                data.value.is_wished = event.want
                candyProductAdapter.notifyItemChanged(data.index)
                break
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
