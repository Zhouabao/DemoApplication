package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VisitorBean
import com.sdy.jitangapplication.presenter.MyVisitPresenter
import com.sdy.jitangapplication.presenter.view.MyVisitView
import com.sdy.jitangapplication.ui.adapter.MyVisitAdater
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_visit.*
import kotlinx.android.synthetic.main.activity_user_center.btnBack
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_visit_headview.view.*

/**
 * 我的访客
 */
class MyVisitActivity : BaseMvpActivity<MyVisitPresenter>(), MyVisitView, OnRefreshListener, OnLoadMoreListener {

    private var page = 1
    private val visitAdapter by lazy { MyVisitAdater() }
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_visit)
        initView()
        mPresenter.myVisitedList(params)
    }

    private fun initView() {
        mPresenter = MyVisitPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        btnBack.onClick {
            finish()
        }
        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myVisitedList(params)
        }

        visitRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        visitRv.adapter = visitAdapter
        visitAdapter.setEmptyView(R.layout.empty_layout, visitRv)
        val view = layoutInflater.inflate(R.layout.item_visit_headview, visitRv, false)
        view.visitTodayCount.text = "今日来访：${intent.getIntExtra("today", 0)}"
        view.visitAllCount.text = "总来访：${intent.getIntExtra("all", 0)}"
        visitAdapter.addHeaderView(view)

        lockToSee.isVisible = !UserManager.isUserVip()
        lockToSee.onClick {
            ChargeVipDialog(this).show()
        }

        visitAdapter.setOnItemClickListener { _, view, position ->
            if (UserManager.isUserVip() && UserManager.getAccid() != visitAdapter.data[position].accid)
                MatchDetailActivity.start(this, visitAdapter.data[position].accid ?: "")
        }

    }

    override fun onMyVisitResult(visitor: MutableList<VisitorBean>?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (visitor.isNullOrEmpty()) {
            refreshLayout.setNoMoreData(true)
        }
        refreshLayout.finishLoadMore(true)
        refreshLayout.finishRefresh(true)
        visitAdapter.addData(visitor ?: mutableListOf())

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
