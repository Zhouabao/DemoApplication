package com.example.demoapplication.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.MyCommentList
import com.example.demoapplication.presenter.MyCommentPresenter
import com.example.demoapplication.presenter.view.MyCommentView
import com.example.demoapplication.ui.adapter.MyCommentAdapter
import com.example.demoapplication.ui.dialog.CommentActionDialog
import com.example.demoapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_my_comment.*
import kotlinx.android.synthetic.main.activity_my_comment.btnBack
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.android.synthetic.main.dialog_comment_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.jetbrains.anko.toast

/**
 * 我的评论
 */
class MyCommentActivity : BaseMvpActivity<MyCommentPresenter>(), MyCommentView, OnRefreshListener, OnLoadMoreListener {

    private val adapter: MyCommentAdapter by lazy { MyCommentAdapter() }

    private var page = 1
    val params: HashMap<String, Any> = hashMapOf(
        "token" to UserManager.getToken(),
        "accid" to UserManager.getAccid(),
        "page" to page,
        "pagesize" to Constants.PAGESIZE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_comment)
        initView()

        mPresenter.myCommentList(params, true)
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        mPresenter = MyCommentPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            params["page"] = page
            mPresenter.myCommentList(params, false)
        }

        commentRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        commentRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, commentRv)

        adapter.setOnItemLongClickListener { adapter, view, position ->
            showCommentDialog(position)
            true
        }
    }

    var commentActionDialog: CommentActionDialog? = null
    private fun showCommentDialog(position: Int) {
        if (commentActionDialog == null) {
            //判断该条评论是不是自己发的
            commentActionDialog = CommentActionDialog(this, "self")
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
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建普通字符串clipData
        val clipData = ClipData.newPlainText("label", "${adapter.data[position].content}")
        //将clipdata内容放到系统剪贴板里
        cm.setPrimaryClip(clipData)
        toast("已复制内容到剪贴板")
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
            ToastUtils.showShort(t.msg)
            adapter.data.removeAt(position)
            adapter.notifyItemRemoved(position)
        } else {
            ToastUtils.showShort("删除失败")
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
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.showSoftInput(publishContent)
        }
    }

}
