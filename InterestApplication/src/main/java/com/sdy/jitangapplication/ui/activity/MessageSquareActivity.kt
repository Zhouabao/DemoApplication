package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SquareMsgBean
import com.sdy.jitangapplication.presenter.MessageSquarePresenter
import com.sdy.jitangapplication.presenter.view.MessageSquareView
import com.sdy.jitangapplication.ui.adapter.MessageSquareAdapter
import com.sdy.jitangapplication.ui.dialog.SquareDelDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_message_square.*
import kotlinx.android.synthetic.main.dialog_square_del.*
import kotlinx.android.synthetic.main.empty_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 发现消息列表
 * /ppsns/tidings/squareListsV3/v1.json  type=1  点赞的  type=2 评论的
 */
class MessageSquareActivity : BaseMvpActivity<MessageSquarePresenter>(), MessageSquareView,
    OnRefreshListener,
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
        //获取广场列表
        mPresenter.squareLists(params)
    }

    private fun initView() {
        btnBack.onClick {
            onBackPressed()
        }
        hotT1.text = "发现消息"
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
        adapter.emptyView.emptyTip.text = "暂时没有消息"
        adapter.isUseEmpty(false)

        //val type: Int? = 0,//类型 1，广场点赞 2，评论我的 3。我的评论点赞的 4 @我的
        adapter.setOnItemClickListener { _, view, position ->
            val item = adapter.data[position]
            when (item.type) {
                1 -> {//点击点赞跳转动态详情
                    //0文本 1图片 2视频 3 语音
                    if (item.category == 0)
                        SquareCommentDetailActivity.start(this, squareId = item.id ?: 0)
                    else
                        SquarePlayListDetailActivity.start(this, item.id ?: 0)
                }
                2, 3 -> {//点击评论进入评论详情
                    SquareCommentDetailActivity.start(this, squareId = item.id ?: 0)
                }

                4 -> { //点击进入活动详情
                    DatingDetailActivity.start2Detail(this, item.id ?: 0)
                }
            }
        }
        adapter.setOnItemLongClickListener { _, view, position ->
            showDel(position)
            true
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.msgIcon -> {
                    if ((adapter.data[position].accid ?: "") != UserManager.getAccid())
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
        his = -1
        unread = -1
        page = 1
        params["page"] = page
        mPresenter.squareLists(params)
    }


    private fun showDel(position: Int) {
        val delDialog = SquareDelDialog(this)
        delDialog.show()
        delDialog.delSquare.onClick {
            val squareBean = adapter.data[position]
            mPresenter.delSquareMsg(
                hashMapOf(
                    "accid" to UserManager.getAccid(),
                    "token" to UserManager.getToken(),
                    "target_accid" to (squareBean.accid ?: ""),
                    "msg_id" to (squareBean.msg_id ?: ""),
                    "type" to (squareBean.type ?: "")
                )
            )
            delDialog.dismiss()
        }
    }

    private var unread = -1
    private var his = -1

    /**
     * 广场列表数据回调
     */
    override fun onSquareListsResult(data: MutableList<SquareMsgBean>?) {
        if (data != null) {
            //进入页面就标记已读
            mPresenter.markSquareRead(params)

            if (data.isNullOrEmpty()) {
                if (refreshLayout.state == RefreshState.Loading)
                    refreshLayout.finishLoadMoreWithNoMoreData()
                else {
                    refreshLayout.finishRefresh()
                    adapter.isUseEmpty(true)
                }
            } else {
                if (refreshLayout.state == RefreshState.Refreshing) {
                    refreshLayout.finishRefresh(true)
                    adapter.data.clear()
                } else {
                    refreshLayout.finishLoadMore(true)
                }
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
                refreshLayout.finishLoadMore(true)
            }
        }
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT

    }


    /**
     * 广场删除回调
     */
    override fun onDelSquareMsgResult(success: Boolean) {
        if (success) {
            refreshLayout.autoRefresh()
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

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
