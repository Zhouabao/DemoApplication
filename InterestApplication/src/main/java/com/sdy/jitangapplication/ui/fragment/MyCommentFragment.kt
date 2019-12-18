package com.sdy.jitangapplication.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshCommentEvent
import com.sdy.jitangapplication.model.MyCommentList
import com.sdy.jitangapplication.presenter.MyCommentPresenter
import com.sdy.jitangapplication.presenter.view.MyCommentView
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity1
import com.sdy.jitangapplication.ui.adapter.MyCommentAdapter
import com.sdy.jitangapplication.ui.dialog.CommentActionDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_comment.*
import kotlinx.android.synthetic.main.dialog_comment_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我的评论
 */
class MyCommentFragment : BaseMvpLazyLoadFragment<MyCommentPresenter>(), MyCommentView, OnRefreshListener,
    OnLoadMoreListener {

    private val adapter: MyCommentAdapter by lazy { MyCommentAdapter() }

    private var page = 1
    val params: HashMap<String, Any> = hashMapOf(
        "token" to UserManager.getToken(),
        "accid" to UserManager.getAccid(),
        "page" to page,
        "pagesize" to Constants.PAGESIZE
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_my_comment, container, false)
    }

    override fun loadData() {
        initView()
        mPresenter.myCommentList(params, true)
    }


    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = MyCommentPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            params["page"] = page
            mPresenter.myCommentList(params, false)
        }

        commentRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        commentRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, commentRv)

        adapter.setOnItemLongClickListener { adapter, view, position ->
            showCommentDialog(position)
            true
        }
        //点击跳转到广场详情
        adapter.setOnItemClickListener { _, view, position ->
            SquareCommentDetailActivity1.start(
                activity!!,
                squareId = adapter.data[position].square_id ?: 0
            )
        }
    }

    var commentActionDialog: CommentActionDialog? = null
    private fun showCommentDialog(position: Int) {
        if (commentActionDialog == null) {
            //判断该条评论是不是自己发的
            commentActionDialog = CommentActionDialog(activity!!, "self")
        }
        commentActionDialog!!.show()
        commentActionDialog!!.copyComment.onClick {
            copyText(position)
            commentActionDialog!!.dismiss()
        }
        commentActionDialog!!.deleteComment.onClick {
            mPresenter.deleteComment(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "id" to adapter.data[position].id!!
                )
                , position
            )
            commentActionDialog!!.dismiss()
        }


        commentActionDialog!!.setOnDismissListener {
            commentActionDialog = null
        }
    }

    private fun copyText(position: Int) {
        //获取剪贴板管理器
        val cm = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建普通字符串clipData
        val clipData = ClipData.newPlainText("label", "${adapter.data[position].content}")
        //将clipdata内容放到系统剪贴板里
        cm.setPrimaryClip(clipData)
        CommonFunction.toast("已复制内容到剪贴板")
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.myCommentList(params, false)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        mPresenter.myCommentList(params, true)
    }


    override fun onGetCommentListResult(data: MyCommentList?, refresh: Boolean) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        adapter.addData(data?.list ?: mutableListOf())
        refreshLayout.finishRefresh(true)
        if (!refresh && data?.list.isNullOrEmpty()) {
            refreshLayout.setNoMoreData(true)
        } else
            refreshLayout.finishLoadMore(true)
    }

    override fun onDeleteCommentResult(t: BaseResp<Any?>?, position: Int) {
        if (t != null && t.code == 200) {
            CommonFunction.toast(t.msg)
            adapter.data.removeAt(position)
            adapter.notifyItemRemoved(position)
        } else {
            CommonFunction.toast("删除失败")
        }

    }


    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCommentEvent(event: RefreshCommentEvent) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        mPresenter.myCommentList(params, true)
    }


}
