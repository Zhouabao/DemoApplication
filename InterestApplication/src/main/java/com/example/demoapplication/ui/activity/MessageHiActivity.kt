package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.UpdateHiEvent
import com.example.demoapplication.model.HiMessageBean
import com.example.demoapplication.presenter.MessageHiPresenter
import com.example.demoapplication.presenter.view.MessageHiView
import com.example.demoapplication.ui.adapter.MessageHiListAdapter
import com.example.demoapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_message_hi.*
import kotlinx.android.synthetic.main.activity_message_hi.stateview
import kotlinx.android.synthetic.main.activity_message_list.btnBack
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 打招呼列表
 */
class MessageHiActivity : BaseMvpActivity<MessageHiPresenter>(), MessageHiView, OnLoadMoreListener, OnRefreshListener {


    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "pagesize" to Constants.PAGESIZE,
            "page" to page
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_hi)
        EventBus.getDefault().register(this)
        initView()
        mPresenter.greatLists(params)
    }


    private val adapter by lazy { MessageHiListAdapter() }
    private fun initView() {
        btnBack.onClick {
            finish()
        }

        mPresenter = MessageHiPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.greatLists(params)
        }

        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        messageHiRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageHiRv.adapter = adapter
        adapter.bindToRecyclerView(messageHiRv)
        adapter.setEmptyView(R.layout.empty_layout, messageHiRv)

        adapter.setOnItemClickListener { _, view, position ->

        }

    }


    override fun onGreatListResult(t: BaseResp<MutableList<HiMessageBean>?>) {
        if (t.data.isNullOrEmpty()) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        refreshLayout.finishRefresh(true)
        adapter.addData(t.data ?: mutableListOf())
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        mPresenter.greatLists(params)

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.greatLists(params)
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
    fun onUpdateHiEvent(event: UpdateHiEvent) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        mPresenter.greatLists(params)
    }

}
