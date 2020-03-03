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
import com.sdy.jitangapplication.event.UpdateFindByTagEvent
import com.sdy.jitangapplication.model.AddLabelBean
import com.sdy.jitangapplication.presenter.FindByTagPresenter
import com.sdy.jitangapplication.presenter.view.FindByTagView
import com.sdy.jitangapplication.ui.adapter.FindLabelAdapter
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_find_by_tag.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 标签找人
 */
class FindByTagFragment : BaseMvpLazyLoadFragment<FindByTagPresenter>(), FindByTagView, OnLoadMoreListener,
    OnRefreshListener {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_by_tag, container, false)
    }


    private val adapter by lazy { FindLabelAdapter() }
    override fun loadData() {
        EventBus.getDefault().register(this)
        mPresenter = FindByTagPresenter()
        mPresenter.mView = this
        stateFindByTag.retryBtn.onClick {
            mPresenter.lookForAllTags()
        }

        refreshFindByTag.setOnLoadMoreListener(this)
        refreshFindByTag.setOnRefreshListener(this)

        rvFindByTag.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvFindByTag.adapter = adapter

        mPresenter.lookForAllTags()

    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.lookForAllTags()
        refreshFindByTag.resetNoMoreData()
    }

    private var page = 1
    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        mPresenter.lookForAllTags()
        refreshFindByTag.resetNoMoreData()
    }

    override fun onTagClassifyListResult(b: Boolean, data: AddLabelBean?) {
        if (b)
            stateFindByTag.viewState = MultiStateView.VIEW_STATE_CONTENT
        else
            stateFindByTag.viewState = MultiStateView.VIEW_STATE_ERROR

        if (refreshFindByTag.state == RefreshState.Refreshing) {
            adapter.data.clear()
            refreshFindByTag.finishRefresh(b)
        } else {
            if ((data?.list ?: mutableListOf()).size < Constants.PAGESIZE)
                refreshFindByTag.finishLoadMoreWithNoMoreData()
            else
                refreshFindByTag.finishLoadMore(b)
        }
        adapter.addData(data?.list ?: mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateFindByTagEvent(eve: UpdateFindByTagEvent) {
        adapter.data.clear()
        mPresenter.lookForAllTags()
    }

}
