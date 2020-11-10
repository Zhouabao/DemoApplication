package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SomeOneGetGiftBean
import com.sdy.jitangapplication.presenter.SomeoneGetGiftPresenter
import com.sdy.jitangapplication.presenter.view.SomeoneGetGiftView
import com.sdy.jitangapplication.ui.adapter.SomeOneGetGiftAdapter
import kotlinx.android.synthetic.main.activity_someone_get_gift.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 某人获取的礼物列表
 */
class SomeoneGetGiftActivity : BaseMvpActivity<SomeoneGetGiftPresenter>(), SomeoneGetGiftView,
    OnRefreshListener, OnLoadMoreListener {

    private val target_accid by lazy { intent.getStringExtra("target_accid") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_someone_get_gift)
        initView()
        mPresenter.getSomeoneGiftList(page, target_accid)
    }

    private val receiveGiftAdapter by lazy { SomeOneGetGiftAdapter() }
    private var page = 1

    private fun initView() {
        mPresenter = SomeoneGetGiftPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick {
            finish()
        }
        hotT1.text = getString(R.string.gift_wall_title)
        stateGift.retryBtn.onClick {
            stateGift.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSomeoneGiftList(page, target_accid)
        }

        refreshGift.setOnRefreshListener(this)
        refreshGift.setOnLoadMoreListener(this)

        rvGift.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvGift.adapter = receiveGiftAdapter
        receiveGiftAdapter.setEmptyView(R.layout.empty_receive_gift_layout, rvGift)
        receiveGiftAdapter.isUseEmpty(false)
        receiveGiftAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.donateAvator -> {
                    MatchDetailActivity.start(this, receiveGiftAdapter.data[position].accid)
                }
            }
        }
    }

    override fun onGetSomeoneGiftList(success: Boolean, data: MutableList<SomeOneGetGiftBean>?) {
        if (success) {
            stateGift.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshGift.state == RefreshState.Loading) {
                receiveGiftAdapter.addData(data ?: mutableListOf())
                if ((data ?: mutableListOf<SomeOneGetGiftBean>()).size < Constants.PAGESIZE)
                    refreshGift.finishLoadMoreWithNoMoreData()
                else
                    refreshGift.finishLoadMore()
            } else {
                receiveGiftAdapter.data.clear()
                receiveGiftAdapter.addData(data ?: mutableListOf())
                receiveGiftAdapter.notifyDataSetChanged()
                receiveGiftAdapter.isUseEmpty(receiveGiftAdapter.data.isNullOrEmpty())
                refreshGift.finishRefresh()
            }
        } else {
            if (refreshGift.state == RefreshState.Loading) {
                refreshGift.finishLoadMore(false)
            } else if (refreshGift.state == RefreshState.Refreshing) {
                refreshGift.finishRefresh(false)
            } else
                stateGift.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        mPresenter.getSomeoneGiftList(page, target_accid)
        refreshGift.resetNoMoreData()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getSomeoneGiftList(page, target_accid)
    }
}
