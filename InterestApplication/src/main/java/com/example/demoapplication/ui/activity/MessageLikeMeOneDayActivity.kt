package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LikeMeOneDayBean
import com.example.demoapplication.presenter.MessageLikeMeOneDayPresenter
import com.example.demoapplication.presenter.view.MessageLikeMeOneDayView
import com.example.demoapplication.ui.adapter.LikeMeOneDayGirdAdapter
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.DividerItemDecoration
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_message_like_me.btnBack
import kotlinx.android.synthetic.main.activity_message_like_me_one_day.*
import kotlinx.android.synthetic.main.activity_message_like_me_one_day.stateview
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 喜欢我的一天总数据
 */
class MessageLikeMeOneDayActivity : BaseMvpActivity<MessageLikeMeOneDayPresenter>(), MessageLikeMeOneDayView,
    OnRefreshListener, OnLoadMoreListener {


    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "pagesize" to Constants.PAGESIZE,
            "page" to page,
            "date" to intent.getStringExtra("date")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_like_me_one_day)
        initView()
        mPresenter.likeListsCategory(params)
    }

    private val adapter by lazy { LikeMeOneDayGirdAdapter() }
    private fun initView() {
        btnBack.onClick {
            finish()
        }

        mPresenter = MessageLikeMeOneDayPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.likeListsCategory(params)
        }

        likeRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        likeRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.BOTH_SET,
                SizeUtils.dp2px(10F),
                this.resources.getColor(R.color.colorWhite)
            )
        )
        likeRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, likeRv)


        likeCount.text = "${intent.getIntExtra("count", 0)} 人对你感兴趣"
        likeDate.text = "${intent.getStringExtra("date")}"
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.likeListsCategory(params)

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        mPresenter.likeListsCategory(params)
    }

    override fun onLikeListResult(datas: MutableList<LikeMeOneDayBean>) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (datas.isNullOrEmpty()) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        adapter.addData(datas)
        refreshLayout.finishRefresh(true)
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        refreshLayout.finishLoadMore(false)
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

}
