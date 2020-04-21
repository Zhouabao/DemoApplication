package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RePublishEvent
import com.sdy.jitangapplication.event.RefreshDeleteSquareEvent
import com.sdy.jitangapplication.event.RefreshLikeEvent
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.presenter.MySquarePresenter
import com.sdy.jitangapplication.presenter.view.MySquareView
import com.sdy.jitangapplication.ui.activity.PublishActivity
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.empty_my_square_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_square.*
import kotlinx.android.synthetic.main.headerview_user_center_square.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 我的广场
 */
class MySquareFragment : BaseMvpLazyLoadFragment<MySquarePresenter>(), MySquareView,
    OnRefreshListener, OnLoadMoreListener {

    private var page = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_square, container, false)
    }

    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "type" to MyCollectionAndLikeFragment.TYPE_MINE,
            "pagesize" to Constants.PAGESIZE
        )
    }
    private val adapter by lazy { RecommendSquareAdapter() }
    override fun loadData() {
        initView()
        mPresenter.aboutMeSquareCandy(params)
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MySquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshMySquare.setOnRefreshListener(this)
        refreshMySquare.setOnLoadMoreListener(this)
        rvMySquare.setHasFixedSize(true)
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        rvMySquare.layoutManager = manager
        rvMySquare.adapter = adapter
        //android 瀑布流
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_my_square_layout, rvMySquare)
        adapter.emptyView.emptyPublishBtn.text = "发布动态"
        adapter.emptyView.emptyPublishBtn.onClick {
            mPresenter.checkBlock()
        }
        adapter.isUseEmpty(false)
    }


    /**
     *头部banner
     */
    private fun initHeadPublish(): View {
        val headPublish = LayoutInflater.from(activity!!)
            .inflate(R.layout.headerview_user_center_square, rvMySquare, false)
        headPublish.publishImg.setImageResource(R.drawable.icon_edit_me)
        headPublish.publishBtn.text = "发布动态"
        headPublish.publishCl.onClick {

            mPresenter.checkBlock()
        }
        return headPublish
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.aboutMeSquareCandy(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.aboutMeSquareCandy(params)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshLikeEvent(event: RefreshLikeEvent) {
        if (event.position != -1 && event.squareId == adapter.data[event.position].id) {
            adapter.data[event.position].isliked = event.isLike == 1
            adapter.data[event.position].like_cnt = if (event.isLike == 1) {
                adapter.data[event.position].like_cnt + 1
            } else {
                adapter.data[event.position].like_cnt - 1
            }

            adapter.refreshNotifyItemChanged(event.position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshDeleteSquareEvent(event: RefreshDeleteSquareEvent) {
        for (data in adapter.data.withIndex()) {
            if (data.value.id == event.squareId) {
                adapter.remove(data.index)
                break
            }
        }
    }

    override fun onGetSquareListResult(data: RecommendSquareListBean?, b: Boolean) {
        if (refreshMySquare.state == RefreshState.Refreshing) {
            if (data?.list.isNullOrEmpty()) {
                adapter.isUseEmpty(true)
            }
            adapter.data.clear()
            adapter.notifyDataSetChanged()
            rvMySquare.scrollToPosition(0)
            refreshMySquare.finishRefresh(b)
        } else {
            if (data?.list.isNullOrEmpty() || (data?.list
                    ?: mutableListOf()).size < Constants.PAGESIZE
            )
                refreshMySquare.finishLoadMoreWithNoMoreData()
            else
                refreshMySquare.finishLoadMore(b)
        }

        if ((data?.list ?: mutableListOf()).size > 0) {
            for (data in data?.list ?: mutableListOf()) {
                data.originalLike = data.isliked
                data.originalLikeCount = data.like_cnt
            }
            adapter.addData(data?.list ?: mutableListOf())
            adapter.addData(mutableListOf())
        }


        if (adapter.data.size == 0) {
            adapter.isUseEmpty(true)
        } else {
            adapter.setHeaderView(initHeadPublish())
            adapter.isUseEmpty(false)
        }

    }

    override fun onCheckBlockResult(b: Boolean) {
        if (b) {
            if (UserManager.publishState == 0) {
                startActivity<PublishActivity>("from" to 2)
            } else
                EventBus.getDefault().post(
                    RePublishEvent(
                        true,
                        UserCenterFragment::class.java.simpleName
                    )
                )
        }
    }
}
