package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.SquareBannerBean
import com.sdy.jitangapplication.presenter.RecommendSquarePresenter
import com.sdy.jitangapplication.presenter.view.RecommendSquareView
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import com.sdy.jitangapplication.ui.holder.BannerHolderView
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_recommend_square.*
import kotlinx.android.synthetic.main.headerview_recommend_banner.view.*

/**
 * 推荐
 */
class RecommendSquareFragment : BaseMvpLazyLoadFragment<RecommendSquarePresenter>(), RecommendSquareView,
    OnRefreshListener, OnLoadMoreListener {

    private var page = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommend_square, container, false)
    }

    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }
    private val adapter by lazy { RecommendSquareAdapter() }
    override fun loadData() {
        initView()
        mPresenter.squareEliteList(params)
    }

    private fun initView() {
        mPresenter = RecommendSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        stateRecommendSquare.retryBtn.onClick {
            //todo 重新连接网络
            stateRecommendSquare.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.squareEliteList(params)
        }
        refreshRecommendSquare.setOnRefreshListener(this)
        refreshRecommendSquare.setOnLoadMoreListener(this)
        rvRecommendSquare.setHasFixedSize(true)
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        rvRecommendSquare.layoutManager = manager
        rvRecommendSquare.adapter = adapter
        //android 瀑布流
        adapter.setHeaderView(initHeadBannerView())
        adapter.setEmptyView(R.layout.empty_friend_layout, rvRecommendSquare)
        adapter.emptyView.emptyFriendTitle.text = "暂时没有人了"
        adapter.emptyView.emptyFriendTip.text = "一会儿再回来看看吧"
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
        adapter.isUseEmpty(false)
        adapter.setOnItemClickListener { _, view, position ->
            SquareCommentDetailActivity.start(activity!!, squareId = adapter.data[position].id)
        }
    }


    /**
     *头部banner
     */
    private fun initHeadBannerView(): View {
        val headBanner =
            LayoutInflater.from(activity!!).inflate(R.layout.headerview_recommend_banner, rvRecommendSquare, false)

        (headBanner.bannerVp2 as BannerViewPager<SquareBannerBean, BannerHolderView>)
            .setHolderCreator { BannerHolderView() }
            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
            .setIndicatorHeight(SizeUtils.dp2px(6f))
            .setOnPageClickListener {
                CommonFunction.toast("$it")
            }
            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
            .create(mutableListOf())

        return headBanner
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.squareEliteList(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.squareEliteList(params)
    }


    override fun onGetSquareRecommendResult(data: RecommendSquareListBean?, b: Boolean) {
        if (b) {
            stateRecommendSquare.viewState = MultiStateView.VIEW_STATE_CONTENT
        } else {
            stateRecommendSquare.viewState = MultiStateView.VIEW_STATE_ERROR
        }
        if (refreshRecommendSquare.state != RefreshState.Loading) {
            if ((data?.banner ?: mutableListOf()).size == 0) {
                adapter.headerLayout.isVisible = false
            } else {
                adapter.headerLayout.isVisible = true
                (adapter.headerLayout.bannerVp2 as BannerViewPager<SquareBannerBean, BannerHolderView>).create(
                    data?.banner ?: mutableListOf()
                )
            }
        }
        if (refreshRecommendSquare.state == RefreshState.Refreshing) {
            if (data?.list.isNullOrEmpty()) {
                adapter.isUseEmpty(true)
            }
            adapter.data.clear()
            refreshRecommendSquare.finishRefresh(b)
        } else {
            if (data?.list.isNullOrEmpty() || (data?.list ?: mutableListOf()).size < Constants.PAGESIZE)
                refreshRecommendSquare.finishLoadMoreWithNoMoreData()
            else
                refreshRecommendSquare.finishLoadMore(b)
        }

        for (data in data?.list ?: mutableListOf()) {
            data.originalLike = data.isliked
            data.originalLikeCount = data.like_cnt
        }
        adapter.addData(data?.list ?: mutableListOf())
        adapter.notifyDataSetChanged()

    }


}
