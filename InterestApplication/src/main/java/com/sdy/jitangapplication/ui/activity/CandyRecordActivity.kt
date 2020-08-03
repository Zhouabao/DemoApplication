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
import com.sdy.jitangapplication.model.BillBean
import com.sdy.jitangapplication.model.MyBillBeans
import com.sdy.jitangapplication.presenter.CandyRecordPresenter
import com.sdy.jitangapplication.presenter.view.CandyRecordView
import com.sdy.jitangapplication.ui.adapter.CandyRecordAdapter
import kotlinx.android.synthetic.main.activity_candy_record.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 交易记录
 */
class CandyRecordActivity : BaseMvpActivity<CandyRecordPresenter>(), CandyRecordView,
    OnRefreshListener, OnLoadMoreListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candy_record)
        initView()
        mPresenter.myRedWithdrawLog(params)
    }

    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    private val adapter by lazy { CandyRecordAdapter() }
    private fun initView() {
        mPresenter = CandyRecordPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.onClick {
            finish()
        }
        hotT1.text = "提现记录"

        stateRecord.retryBtn.onClick {
            stateRecord.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myRedWithdrawLog(params)
        }

        refreshRecord.setOnRefreshListener(this)
        refreshRecord.setOnLoadMoreListener(this)

        rvRecord.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvRecord.adapter = adapter

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.myRedWithdrawLog(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.myRedWithdrawLog(params)
    }

    override fun onMyRedWithdrawLog(success: Boolean, billList: MyBillBeans?) {
        if (success) {
            stateRecord.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshRecord.state != RefreshState.Loading) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                refreshRecord.finishRefresh()
                refreshRecord.resetNoMoreData()
            } else {
                if ((billList?.list ?: mutableListOf<BillBean>()).size < Constants.PAGESIZE) {
                    refreshRecord.finishLoadMoreWithNoMoreData()
                } else {
                    refreshRecord.finishLoadMore(true)
                }
            }
            adapter.addData(billList?.list?: mutableListOf())

            if (adapter.data.isEmpty()) {
                stateRecord.viewState = MultiStateView.VIEW_STATE_EMPTY
            }
        } else {
            stateRecord.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }
}
