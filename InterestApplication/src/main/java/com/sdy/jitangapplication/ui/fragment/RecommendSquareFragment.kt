package com.sdy.jitangapplication.ui.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshSquareByGenderEvent
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.SquareBannerBean
import com.sdy.jitangapplication.presenter.RecommendSquarePresenter
import com.sdy.jitangapplication.presenter.view.RecommendSquareView
import com.sdy.jitangapplication.ui.activity.PublishActivity
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import com.sdy.jitangapplication.ui.holder.BannerHolderView
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_recommend_square.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_recommend_banner.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

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
            "pagesize" to Constants.PAGESIZE,
            "gender" to SPUtils.getInstance(Constants.SPNAME).getInt("filter_square_gender", 3)

        )
    }
    private val adapter by lazy { RecommendSquareAdapter() }
    override fun loadData() {
        initView()
        mPresenter.squareEliteList(params)
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = RecommendSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        stateRecommendSquare.retryBtn.onClick {
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
                if (banner[it].adv_type == 2) {//广告类型默认1   1.只是展示图  2.跳转外连  3.内部跳转   4发布+话题 5发布+兴趣
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    intent.data = Uri.parse(banner[it].cover_url)//此处填链接
                    startActivity(intent)
                } else if (banner[it].adv_type == 4) {//4发布+话题
                    mPresenter.checkBlock(banner[it])

                } else if (banner[it].adv_type == 5) {//5发布+兴趣

                }
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
        params["gender"] = SPUtils.getInstance(Constants.SPNAME).getInt("filter_square_gender", 3)
        mPresenter.squareEliteList(params)
    }


    private var banner: MutableList<SquareBannerBean> = mutableListOf()
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
                banner = data?.banner ?: mutableListOf()
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

    override fun onCheckBlockResult(bannerBean: SquareBannerBean, b: Boolean) {
        if (b)
            when (bannerBean.adv_type) {
                4 -> {
                    startActivity<PublishActivity>("title" to bannerBean.title)
                }
                5 -> {
                    startActivity<PublishActivity>("tag_id" to bannerBean.id)
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareByGenderEvent(event: RefreshSquareByGenderEvent) {
        refreshLayout.autoRefresh()
    }


}
