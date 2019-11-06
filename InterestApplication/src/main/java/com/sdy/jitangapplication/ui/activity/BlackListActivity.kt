package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.friend.FriendService
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateBlackEvent
import com.sdy.jitangapplication.model.BlackBean
import com.sdy.jitangapplication.presenter.BlackListPresenter
import com.sdy.jitangapplication.presenter.view.BlackListView
import com.sdy.jitangapplication.ui.adapter.BlackListAdapter
import com.sdy.jitangapplication.ui.dialog.ResolveBlackDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_black_list.*
import kotlinx.android.synthetic.main.dialog_resolve_black.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 黑名单
 */
class BlackListActivity : BaseMvpActivity<BlackListPresenter>(), BlackListView, OnRefreshListener, OnLoadMoreListener {


    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }
    private val adapter by lazy { BlackListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black_list)
        EventBus.getDefault().register(this)
        initView()


        refreshLayout.autoRefresh()
    }

    private fun initView() {
        btnBack.onClick { finish() }
        hotT1.text = "黑名单"
        mPresenter = BlackListPresenter()
        mPresenter.mView = this
        mPresenter.context = this


        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myShieldingList(params)
        }

        blackList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        blackList.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, blackList)
        adapter.setOnItemLongClickListener { _, view, position ->
            showBlackDialog(position)
            true
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.friendIcon -> {
                    MatchDetailActivity.start(this, adapter.data[position].accid)
                }
            }
        }

    }

    private val dialog by lazy { ResolveBlackDialog(this) }


    private fun showBlackDialog(position: Int) {
        dialog.show()
        dialog.llLahei.onClick {
            mPresenter.removeBlock(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "target_accid" to adapter.data[position].accid
                ), position
            )
            dialog.dismiss()
        }
        dialog.cancel.onClick {
            dialog.dismiss()
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.myShieldingList(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        mPresenter.myShieldingList(params)
    }

    override fun onRemoveBlockResult(success: Boolean, position: Int) {
        if (success) {
            CommonFunction.toast("解除拉黑成功！")
            NIMClient.getService(FriendService::class.java).removeFromBlackList(adapter.data[position].accid)
            adapter.remove(position)
        }
    }


    override fun onMyShieldingListResult(data: MutableList<BlackBean>?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data != null) {
            adapter.addData(data)
        }
        refreshLayout.finishRefresh()
        if (adapter.data.size < page * Constants.PAGESIZE)
            refreshLayout.finishLoadMoreWithNoMoreData()
        else
            refreshLayout.finishLoadMore()
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        if (text.isEmpty())
            stateview.errorMsg.text = CommonFunction.getErrorMsg(this)
        else
            stateview.errorMsg.text = text
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateBlackListEvent(update: UpdateBlackEvent) {
        refreshLayout.autoRefresh()
    }

}
