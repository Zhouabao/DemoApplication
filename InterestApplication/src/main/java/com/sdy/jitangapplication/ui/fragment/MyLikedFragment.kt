package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshCommentEvent
import com.sdy.jitangapplication.model.MyLikedBean
import com.sdy.jitangapplication.presenter.MyLikedPresenter
import com.sdy.jitangapplication.presenter.view.MyLikedView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MyLikedAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_comment.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我喜欢过的人
 */
class MyLikedFragment : BaseMvpLazyLoadFragment<MyLikedPresenter>(), MyLikedView, OnRefreshListener,
    OnLoadMoreListener {

    private val adapter: MyLikedAdapter by lazy { MyLikedAdapter() }

    private var page = 1
    val params: HashMap<String, Any> = hashMapOf(
        "token" to UserManager.getToken(),
        "accid" to UserManager.getAccid(),
        "page" to page,
        "pagesize" to Constants.PAGESIZE
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_my_comment, container, false)
    }

    override fun loadData() {
        initView()
        mPresenter.myLikedLis(params, true)
    }


    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = MyLikedPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            params["page"] = page
            mPresenter.myLikedLis(params, false)
        }

        commentRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        commentRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, commentRv)

        //点击跳转到广场详情
        adapter.setOnItemClickListener { _, view, position ->
            MatchDetailActivity.start(activity!!, adapter.data[position].accid)
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.myLikedLis(params, false)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        refreshLayout.resetNoMoreData()
        mPresenter.myLikedLis(params, true)
    }


    override fun onGetCommentListResult(data: MutableList<MyLikedBean>?, refresh: Boolean) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (refreshLayout.state == RefreshState.Refreshing) {
            adapter.data.clear()
            adapter.notifyDataSetChanged()
            refreshLayout.finishRefresh(true)
        }
        if ((data ?: mutableListOf()).size < Constants.PAGESIZE) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        adapter.addData(data ?: mutableListOf())

        if (adapter.data.isEmpty()) {
            adapter.isUseEmpty(true)
        }
    }


    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCommentEvent(event: RefreshCommentEvent) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        mPresenter.myLikedLis(params, true)
    }


}
