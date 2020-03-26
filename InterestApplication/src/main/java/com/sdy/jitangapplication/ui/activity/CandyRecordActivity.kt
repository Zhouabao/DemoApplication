package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
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
    }

    private val adapter by lazy { CandyRecordAdapter() }
    private fun initView() {

        btnBack.onClick {
            finish()
        }
        hotT1.text="交易记录"

        stateRecord.retryBtn.onClick {
            //todo 重新请求
        }

        refreshRecord.setOnRefreshListener(this)
        refreshRecord.setOnLoadMoreListener(this)


        rvRecord.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvRecord.adapter = adapter

        for (i in 0 until 10) {
            adapter.addData("$i")
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {

    }
}
