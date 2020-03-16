package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.SPUtils
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
import com.sdy.jitangapplication.model.TagSquareListBean
import com.sdy.jitangapplication.presenter.TagDetailCategoryPresenter
import com.sdy.jitangapplication.presenter.view.TagDetailCategoryView
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_nearby_square.*
import kotlinx.android.synthetic.main.fragment_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 附近广场
 */
class NearbySquareFragment : BaseMvpLazyLoadFragment<TagDetailCategoryPresenter>(), TagDetailCategoryView,
    OnRefreshListener, OnLoadMoreListener {


    private var page = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_nearby_square, container, false)
    }

    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "gender" to SPUtils.getInstance(Constants.SPNAME).getInt("filter_square_gender", 3),
            "type" to 3
        )
    }
    private val adapter by lazy { RecommendSquareAdapter() }
    override fun loadData() {
        initView()
        mPresenter.squareTagInfo(params)
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = TagDetailCategoryPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        stateNearbySquare.retryBtn.onClick {
            stateNearbySquare.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.squareTagInfo(params)
        }
        refreshNearbySquare.setOnRefreshListener(this)
        refreshNearbySquare.setOnLoadMoreListener(this)

        rvNearbySquare.setHasFixedSize(true)
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        rvNearbySquare.layoutManager = manager
        rvNearbySquare.adapter = adapter
        adapter.setEmptyView(R.layout.empty_friend_layout, rvNearbySquare)
        adapter.emptyView.emptyFriendTitle.text = "暂时没有人了"
        adapter.emptyView.emptyFriendTip.text = "一会儿再回来看看吧"
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
        adapter.isUseEmpty(false)
        adapter.setOnItemClickListener { _, view, position ->
            SquareCommentDetailActivity.start(activity!!, squareId = adapter.data[position].id)
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.squareTagInfo(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        params["gender"] = SPUtils.getInstance(Constants.SPNAME).getInt("filter_square_gender", 3)
        mPresenter.squareTagInfo(params)
    }

    override fun onGetSquareRecommendResult(data: TagSquareListBean?, b: Boolean) {
        if (b) {
            stateNearbySquare.viewState = MultiStateView.VIEW_STATE_CONTENT
        } else {
            stateNearbySquare.viewState = MultiStateView.VIEW_STATE_ERROR
        }
        if (refreshNearbySquare.state == RefreshState.Refreshing) {
            if (data?.list.isNullOrEmpty()) {
                adapter.isUseEmpty(true)
            }
            adapter.data.clear()
            refreshNearbySquare.finishRefresh(b)
        } else {
            if (data?.list.isNullOrEmpty() || (data?.list ?: mutableListOf()).size < Constants.PAGESIZE)
                refreshNearbySquare.finishLoadMoreWithNoMoreData()
            else
                refreshNearbySquare.finishLoadMore(b)
        }

        for (data in data?.list ?: mutableListOf()) {
            data.originalLike = data.isliked
            data.originalLikeCount = data.like_cnt
        }
        adapter.addData(data?.list ?: mutableListOf())
        adapter.notifyDataSetChanged()
    }

    override fun onCheckBlockResult(b: Boolean) {}


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareByGenderEvent(event: RefreshSquareByGenderEvent) {
        refreshLayout.autoRefresh()
    }

}
