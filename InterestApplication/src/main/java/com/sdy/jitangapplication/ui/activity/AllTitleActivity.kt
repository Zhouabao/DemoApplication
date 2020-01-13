package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.TopicBean
import com.sdy.jitangapplication.presenter.AllTitlePresenter
import com.sdy.jitangapplication.presenter.view.AllTitleView
import com.sdy.jitangapplication.ui.adapter.AllTitleAdapter
import com.sdy.jitangapplication.ui.adapter.AllTitleNavAdapter
import kotlinx.android.synthetic.main.activity_all_title.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 全部标题
 */
class AllTitleActivity : BaseMvpActivity<AllTitlePresenter>(), AllTitleView, OnRefreshLoadMoreListener {

    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_title)
        initView()
        mPresenter.getTitleMenuList()
    }

    private var choosedId = 0
    private val titleNavAdapter by lazy { AllTitleNavAdapter() }
    private val titlesAdapter by lazy { AllTitleAdapter() }
    private fun initView() {
        mPresenter = AllTitlePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshTitle.setOnRefreshLoadMoreListener(this)


        btnBack.onClick {
            finish()
        }
        hotT1.text = "更多标题"
        divider.isVisible = false
        titleClassRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        titleClassRv.adapter = titleNavAdapter
        titleNavAdapter.setOnItemClickListener { _, view, position ->
            view.isEnabled = false
            for (data in titleNavAdapter.data) {
                data.isfuse = data == titleNavAdapter.data[position]
                choosedId = titleNavAdapter.data[position].id
            }
            titleNavAdapter.notifyDataSetChanged()
            page = 1
            titlesAdapter.data.clear()
            stateTitle.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getTitleLists(page, choosedId)
            view.postDelayed({ view.isEnabled = true }, 1000L)
        }

        titlesRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        titlesRv.adapter = titlesAdapter
        titlesAdapter.setEmptyView(R.layout.empty_layout, titlesRv)
        titlesAdapter.isUseEmpty(false)
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getTitleLists(page, choosedId)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        titlesAdapter.data.clear()
        mPresenter.getTitleLists(page, choosedId)
    }


    override fun onGetTitleMenuListResult(b: Boolean, data: MutableList<LabelQualityBean>?) {
        if (b) {
            if (!data.isNullOrEmpty()) {
                data[0].isfuse = true
                titleNavAdapter.setNewData(data ?: mutableListOf())
                choosedId = data[0].id
                mPresenter.getTitleLists(page, data[0].id)
            }
        } else {
            stateTitle.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }

    override fun onGetTitleListsResult(b: Boolean, data: MutableList<TopicBean>?) {
        if (refreshTitle.state == RefreshState.Loading) {
            if (data.isNullOrEmpty() && b) {
                refreshTitle.finishLoadMoreWithNoMoreData()
            } else {
                refreshTitle.finishLoadMore(b)
            }
        } else if (refreshTitle.state == RefreshState.Refreshing) {
            refreshTitle.finishRefresh(b)
        }

        if (b) {
            stateTitle.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data.isNullOrEmpty()) {
                titlesAdapter.isUseEmpty(true)
            } else {
                titlesAdapter.addData(data ?: mutableListOf())
                titlesAdapter.isUseEmpty(false)
            }
        } else {
            stateTitle.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

}
