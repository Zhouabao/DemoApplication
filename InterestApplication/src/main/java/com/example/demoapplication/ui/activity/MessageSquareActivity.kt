package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.SquareMsgBean
import com.example.demoapplication.presenter.MessageSquarePresenter
import com.example.demoapplication.presenter.view.MessageSquareView
import com.example.demoapplication.ui.adapter.MessageSquareAdapter
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.DividerItemDecoration
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_message_square.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 发现消息列表
 */
class MessageSquareActivity : BaseMvpActivity<MessageSquarePresenter>(), MessageSquareView, OnRefreshListener,
    OnLoadMoreListener {


    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }
    private val adapter by lazy { MessageSquareAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_square)
        initView()
        mPresenter.squareLists(params)
        mPresenter.markSquareRead(params)
    }

    private fun initView() {
        llTitle.onClick {
            finish()
        }
        mPresenter = MessageSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.squareLists(params)
        }

        messageSquareNewRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageSquareNewRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(10F),
                resources.getColor(R.color.colorWhite)
            )
        )
        messageSquareNewRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, messageSquareNewRv)

        //val type: Int? = 0,//类型 1，广场点赞 2，评论我的 3。我的评论点赞的 4 @我的
        adapter.setOnItemClickListener { _, view, position ->
            val item = adapter.data[position]
            when (item.type) {
                1 -> {//点击点赞跳转动态详情
                    SquarePlayListDetailActivity.start(this, item.id ?: 0)
                }
                2,3 -> {//点击评论进入评论详情
                    SquareCommentDetailActivity.start(this, squareId =  item.id ?: 0, enterPosition = "comment")
                }
            }
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.msgIcon -> {
                    MatchDetailActivity.start(this, adapter.data[position].accid ?: "")
                }
            }
        }


    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.squareLists(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        adapter.data.clear()
        his = -1
        unread = -1
        page = 1
        params["page"] = page
        mPresenter.squareLists(params)
    }

    private var unread = -1
    private var his = -1
    override fun onSquareListsResult(data: MutableList<SquareMsgBean>?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data != null) {
            refreshLayout.finishRefresh(true)
            if (data.isNullOrEmpty()) {
                refreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                refreshLayout.finishLoadMore(true)
                for (msg in data.withIndex()) {
                    if (msg.value.is_read == false && unread == -1) {
                        msg.value.pos = 0
                        unread = 0
                    }
                    if (msg.value.is_read == true && his == -1) {
                        msg.value.pos = 0
                        his = 0
                    }
                }
                adapter.addData(data)
            }
        }
    }

    override fun onError(text: String) {
        refreshLayout.finishRefresh(false)
        refreshLayout.finishLoadMore(false)

        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }
}
