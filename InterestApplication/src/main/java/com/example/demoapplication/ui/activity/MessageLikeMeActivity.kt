package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LikeMeBean
import com.example.demoapplication.presenter.MessageLikeMePresenter
import com.example.demoapplication.presenter.view.MessageLikeMeView
import com.example.demoapplication.ui.adapter.LikeMeAdapter
import com.example.demoapplication.ui.dialog.ChargeVipDialog
import com.example.demoapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_message_like_me.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.jetbrains.anko.startActivity

/**
 * 对我感兴趣的
 */
class MessageLikeMeActivity : BaseMvpActivity<MessageLikeMePresenter>(), MessageLikeMeView, OnRefreshListener,
    OnLoadMoreListener, View.OnClickListener {


    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_like_me)
        initView()
        mPresenter.likeLists(params)
    }

    private val adapter by lazy { LikeMeAdapter() }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        lockToSee.setOnClickListener(this)
        lockToSee.visibility = if (UserManager.isUserVip()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        mPresenter = MessageLikeMePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        likeMeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        likeMeRv.adapter = adapter


        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.likeMeCount -> {
                    startActivity<MessageLikeMeOneDayActivity>(
                        "date" to "${adapter.data[position].date}",
                        "count" to adapter.data[position].count
                    )
                }
            }
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.likeLists(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        page = 1
        params["page"] = page
        mPresenter.likeLists(params)
    }


    override fun onLikeListsResult(data: MutableList<LikeMeBean>) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data.size < Constants.PAGESIZE) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        adapter.addData(data)
        refreshLayout.finishRefresh()
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lockToSee->{
                ChargeVipDialog(this).show()
            }
        }


    }

    override fun onBackPressed() {
        mPresenter.markLikeRead(params)
        super.onBackPressed()
    }
}
