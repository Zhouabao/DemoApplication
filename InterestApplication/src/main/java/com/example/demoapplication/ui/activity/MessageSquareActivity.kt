package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.SquareLitBean
import com.example.demoapplication.presenter.MessageSquarePresenter
import com.example.demoapplication.presenter.view.MessageSquareView
import com.example.demoapplication.ui.adapter.MessageSquareAdapter
import com.example.demoapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_message_list.btnBack
import kotlinx.android.synthetic.main.activity_message_square.*
import kotlinx.android.synthetic.main.activity_message_square.stateview
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_square_headview.view.*

/**
 * 发现消息列表
 */
class MessageSquareActivity : BaseMvpActivity<MessageSquarePresenter>(), MessageSquareView, OnRefreshListener,
    OnLoadMoreListener {


    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }
    private val adapter by lazy { MessageSquareAdapter() }
    private val historyAdapter by lazy { MessageSquareAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_square)
        initView()
        mPresenter.squareLists(params)
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        mPresenter = MessageSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.squareLists(params)
        }

        messageSquareNewRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageSquareNewRv.adapter = adapter
//        headTitle
        val headNewView = LayoutInflater.from(this).inflate(R.layout.item_square_headview, messageSquareNewRv, false)
        headNewView.headTitle.text = "未读消息"
        adapter.addHeaderView(headNewView)
        adapter.headerLayout.visibility = View.GONE


        messageSquareHistoryRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageSquareHistoryRv.adapter = historyAdapter
        val headHisView =
            LayoutInflater.from(this).inflate(R.layout.item_square_headview, messageSquareHistoryRv, false)
        headHisView.headTitle.text = "历史消息"
        historyAdapter.addHeaderView(headHisView)
        historyAdapter.headerLayout.visibility = View.GONE


    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.squareLists(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.squareLists(params)
    }

    override fun onSquareListsResult(data: SquareLitBean?) {
        if (data != null) {
            if (adapter.data.size == 0 && data.newest.isNullOrEmpty()) {
                adapter.headerLayout.visibility = View.GONE
            } else {
                adapter.headerLayout.visibility = View.VISIBLE
            }
            if (historyAdapter.data.size == 0 && data.history.isNullOrEmpty()) {
                historyAdapter.headerLayout.visibility = View.GONE
            } else {
                historyAdapter.headerLayout.visibility = View.VISIBLE
            }
            if (data.history.isNullOrEmpty() && data.newest.isNullOrEmpty()) {
                refreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                refreshLayout.finishLoadMore(true)
            }
            adapter.addData(data.newest ?: mutableListOf())
            historyAdapter.addData(data.history ?: mutableListOf())
        }
        refreshLayout.finishRefresh(true)
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
