package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.presenter.AccostListPresenter
import com.sdy.jitangapplication.presenter.view.AccostListView
import com.sdy.jitangapplication.ui.adapter.AccostListAdapter
import kotlinx.android.synthetic.main.activity_accost_list.*

/**
 * 全部搭讪
 */
class AccostListActivity : BaseMvpActivity<AccostListPresenter>(), AccostListView,
    OnRefreshLoadMoreListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accost_list)
        initView()
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

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {


    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
    }
}
