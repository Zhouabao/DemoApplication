package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.VisitorBean
import com.sdy.jitangapplication.presenter.MyVisitPresenter
import com.sdy.jitangapplication.presenter.view.MyVisitView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.MyVisitActivity
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import com.sdy.jitangapplication.ui.adapter.MyVisitAdater
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_visit.*
import kotlinx.android.synthetic.main.item_visit_headview.view.*
import org.jetbrains.anko.support.v4.startActivity

/**
 *
 * 我所有的访客
 */
class MyVisitFragment(
    val freeShow: Boolean,
    val from: Int = MyVisitActivity.FROM_ME,
    val today: Int = 0,
    val all: Int = 0
) :
    BaseMvpLazyLoadFragment<MyVisitPresenter>(),
    MyVisitView, OnRefreshListener,
    OnLoadMoreListener {
    private var page = 1

    //    private val visitAdapter by lazy { MyVisitAdater(intent.getBooleanExtra("freeShow", false)) }
    private val visitAdapter by lazy { MyVisitAdater(freeShow) }
    private val params by lazy {
        hashMapOf<String,Any>(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "type" to 2
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_visit, container, false)
    }


    override fun loadData() {
        initView()
        mPresenter.myVisitedList(params)
    }

    private fun initView() {
        mPresenter = MyVisitPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myVisitedList(params)
        }

        visitRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        visitRv.adapter = visitAdapter
        visitAdapter.setEmptyView(R.layout.empty_layout, visitRv)
        if (from == MyVisitActivity.FROM_ME) {
            val view = layoutInflater.inflate(R.layout.item_visit_headview, visitRv, false)
            view.visitTodayCount.text = "今日来访：${today}"
            view.visitAllCount.text = "总来访：${all}"
        }

        visitAdapter.isUseEmpty(false)

        lockToSee.isVisible = !visitAdapter.freeShow
        lockToSee.onClick {
            CommonFunction.startToVip(activity!!,VipPowerActivity.SOURCE_VISITED_ME)
        }

        visitAdapter.setOnItemClickListener { _, view, position ->
            if (visitAdapter.freeShow && UserManager.getAccid() != visitAdapter.data[position].accid)
                MatchDetailActivity.start(activity!!, visitAdapter.data[position].accid ?: "")
        }

    }

    override fun onMyVisitResult(visitor: MutableList<VisitorBean>?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (visitor.isNullOrEmpty()) {
            refreshLayout.setNoMoreData(true)
            if (page == 1) {
                lockToSee.isVisible = false
            }
        }
        refreshLayout.finishLoadMore(true)
        refreshLayout.finishRefresh(true)
        visitAdapter.addData(visitor ?: mutableListOf())

        if (visitAdapter.data.isNullOrEmpty()) {
            visitAdapter.isUseEmpty(true)
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.myVisitedList(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        visitAdapter.data.clear()
        refreshLayout.setNoMoreData(false)
        page = 1
        params["page"] = page
        mPresenter.myVisitedList(params)
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

}