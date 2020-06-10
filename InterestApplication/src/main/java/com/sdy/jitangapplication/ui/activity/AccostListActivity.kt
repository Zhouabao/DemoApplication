package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.presenter.AccostListPresenter
import com.sdy.jitangapplication.presenter.view.AccostListView
import com.sdy.jitangapplication.ui.adapter.AccostListAdapter
import kotlinx.android.synthetic.main.activity_accost_list.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 全部搭讪
 */
class AccostListActivity : BaseMvpActivity<AccostListPresenter>(), AccostListView,
    OnRefreshLoadMoreListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accost_list)
        initView()
        mPresenter.chatupList(params)
    }

    private val adapter by lazy { AccostListAdapter() }
    private fun initView() {
        mPresenter = AccostListPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshAccost.setOnRefreshLoadMoreListener(this)

        rvAccost.layoutManager = LinearLayoutManager(this)
        rvAccost.adapter = adapter


    }

    override fun onChatupListResult(data: MutableList<AccostBean>?) {
        //获取最近联系人列表
        mPresenter.getRecentContacts(data?: mutableListOf())
    }


    override fun onGetRecentContactResults(
        contacts: MutableList<RecentContact>,
        t: MutableList<AccostBean>
    ) {
        if (t.isNullOrEmpty()) {
            refreshAccost.finishLoadMoreWithNoMoreData()
        } else {
            refreshAccost.finishLoadMore(true)
        }
        if (refreshAccost.state == RefreshState.Refreshing) {
            adapter.data.clear()
            refreshAccost.finishRefresh(true)
        }
        for (recentContactt in contacts) {
            for (data in t) {
                if (recentContactt.contactId == data.accid) {
                    data.unreadCnt = recentContactt.unreadCount
                    data.time = recentContactt.time
                }
            }
        }
        adapter.addData(t)
//        adapter.notifyDataSetChanged()
        adapter.isUseEmpty(adapter.data.isEmpty())
        stateAccost.viewState = MultiStateView.VIEW_STATE_CONTENT

    }


    private var page = 1
    private val params by lazy { hashMapOf<String, Any>() }
    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.chatupList(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.chatupList(params)
    }


    override fun onError(text: String) {
        stateAccost.viewState = MultiStateView.VIEW_STATE_ERROR
        stateAccost.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        registerObservers(false)
    }


    /**
     * ********************** 收消息，处理状态变化 ************************
     */
    private fun registerObservers(register: Boolean) {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(messageReceiverObserver, register)
    }


    //监听在线消息中是否有@我
    private val messageReceiverObserver =
        Observer<List<IMMessage>> { imMessages ->
            if (imMessages != null) {
                for (contact in adapter.data.withIndex()) {
                    for (imMessage in imMessages) {
                        if (contact.value.accid == imMessage.fromAccount) {
                            contact.value.unreadCnt = contact.value.unreadCnt + 1
                            contact.value.time = imMessage.time
                            adapter.notifyItemChanged(contact.index)
                        }
                    }
                }
            }
        }
}
